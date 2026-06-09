package model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import model.domain.exception.InvalidHabitException;
import model.domain.model.AbstractTask;
import model.domain.model.DailyHabit;
import model.domain.model.OneTimeTask;
import model.domain.model.User;
import model.domain.model.WeeklyHabit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dienst für den Im- und Export der Benutzerdaten.
 * <p>
 * Erstellt verschlüsselte JSON-Sicherungen (AES/GCM mit aus einem Passwort
 * abgeleitetem Schlüssel), liest sie wieder ein, exportiert die Gewohnheiten
 * als CSV und bereitet einen Ordner für die Cloud-Synchronisierung vor.
 */
@Singleton
public class DataPortabilityService {
    private static final Logger logger = LoggerFactory.getLogger(DataPortabilityService.class);
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 65_536;
    private static final int IV_BYTES = 12;
    private static final int SALT_BYTES = 16;
    private static final int GCM_TAG_BITS = 128;
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Erzeugt den Dienst mit Zugriff auf die Benutzerdaten.
     *
     * @param userService der Benutzerdienst (liefert die zu sichernden Daten)
     */
    @Inject
    public DataPortabilityService(UserService userService) {
        this.userService = userService;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exportiert die Benutzerdaten als verschlüsselte JSON-Sicherung.
     *
     * @param outputFile die Zieldatei
     * @return der Pfad der erstellten Sicherung
     * @throws IllegalStateException wenn der Export fehlschlägt
     */
    public Path exportEncryptedJson(Path outputFile) {
        try {
            User user = userService.getUser();
            BackupPayload payload = buildPayload(user);
            String json = objectMapper.writeValueAsString(payload);
            EncryptedPayload encrypted = encrypt(json, resolveEncryptionPassword());
            Files.createDirectories(outputFile.toAbsolutePath().getParent());
            objectMapper.writeValue(outputFile.toFile(), encrypted);
            return outputFile;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to export encrypted JSON backup", exception);
        }
    }

    /**
     * Liest eine verschlüsselte JSON-Sicherung ein, übernimmt deren Inhalt in
     * den Benutzer und speichert die Daten anschließend.
     *
     * @param encryptedBackupFile die Sicherungsdatei
     * @throws IllegalStateException wenn der Import fehlschlägt
     */
    public void importEncryptedJson(Path encryptedBackupFile) {
        try {
            EncryptedPayload encrypted = objectMapper.readValue(encryptedBackupFile.toFile(), EncryptedPayload.class);
            String json = decrypt(encrypted, resolveEncryptionPassword());
            BackupPayload payload = objectMapper.readValue(json, BackupPayload.class);
            applyPayload(payload);
            userService.writeTaskData();
            userService.writeUserData();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to import encrypted JSON backup", exception);
        }
    }

    /**
     * Exportiert die Gewohnheiten des Benutzers als CSV-Datei.
     *
     * @param outputFile die Zieldatei
     * @return der Pfad der erstellten CSV-Datei
     * @throws IllegalStateException wenn der Export fehlschlägt
     */
    public Path exportCsv(Path outputFile) {
        try {
            User user = userService.getUser();
            Files.createDirectories(outputFile.toAbsolutePath().getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                writer.write("type,name,description,points,completed,streak");
                writer.newLine();
                for (AbstractTask task : user.getTasks()) {
                    String type = task instanceof DailyHabit ? "DAILY"
                            : task instanceof WeeklyHabit ? "WEEKLY" : "ONE_TIME";
                    writer.write(csv(type));
                    writer.write(",");
                    writer.write(csv(task.getName()));
                    writer.write(",");
                    writer.write(csv(task.getDescription()));
                    writer.write(",");
                    writer.write(String.valueOf(task.calculatePoints()));
                    writer.write(",");
                    writer.write(String.valueOf(task.isCompleted()));
                    writer.write(",");
                    writer.write(String.valueOf(task.getStreak()));
                    writer.newLine();
                }
            }
            return outputFile;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to export CSV", exception);
        }
    }

    /**
     * Stellt sicher, dass der Ordner für die Cloud-Synchronisierung existiert.
     *
     * @return der Pfad des vorbereiteten Ordners
     * @throws IllegalStateException wenn der Ordner nicht angelegt werden kann
     */
    public Path ensureCloudSyncPreparationPath() {
        try {
            Path syncPath = Path.of("data", "cloud-sync", "dropbox-ready").toAbsolutePath();
            Files.createDirectories(syncPath);
            return syncPath;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare cloud sync path", exception);
        }
    }

    /**
     * Erstellt eine verschlüsselte Sicherung mit Zeitstempel im
     * Synchronisierungsordner.
     *
     * @return der Pfad der erstellten Sicherung
     */
    public Path createTimestampedEncryptedBackupInSyncFolder() {
        Path folder = ensureCloudSyncPreparationPath();
        Path output = folder.resolve("disciplica-backup-" + LocalDateTime.now().format(FILE_TS) + ".json.enc");
        return exportEncryptedJson(output);
    }

    /**
     * Baut aus dem aktuellen Benutzer das zu sichernde Datenpaket.
     *
     * @param user der Benutzer
     * @return das gefüllte Sicherungspaket
     */
    private BackupPayload buildPayload(User user) {
        List<TaskRecord> taskRecords = new ArrayList<>();
        for (AbstractTask task : user.getTasks()) {
            String type = task instanceof DailyHabit ? "DAILY"
                    : task instanceof WeeklyHabit ? "WEEKLY" : "ONE_TIME";
            taskRecords.add(new TaskRecord(type, task.getName(), task.getDescription(), task.getPoints(), task.isCompleted(), task.getStreak()));
        }
        return new BackupPayload(
                "1.0",
                LocalDateTime.now().toString(),
                user.getUsername(),
                user.getLevel(),
                user.getExperience(),
                user.getGold(),
                user.getHealth(),
                user.getTitle(),
                taskRecords,
                new HashMap<>(user.getCompletionsByDateSnapshot()),
                new ArrayList<>(user.getXpHistorySnapshot())
        );
    }

    /**
     * Übernimmt ein eingelesenes Sicherungspaket in den aktuellen Benutzer.
     *
     * @param payload das eingelesene Sicherungspaket
     * @throws InvalidHabitException wenn eine enthaltene Aufgabe ungültig ist
     */
    private void applyPayload(BackupPayload payload) throws InvalidHabitException {
        List<AbstractTask> tasks = new ArrayList<>();
        for (TaskRecord record : payload.tasks()) {
            AbstractTask task;
            if ("DAILY".equals(record.type())) {
                DailyHabit daily = new DailyHabit(record.name(), record.description(), record.points());
                daily.setStreak(record.streak());
                task = daily;
            } else if ("WEEKLY".equals(record.type())) {
                WeeklyHabit weekly = new WeeklyHabit(record.name(), record.description(), record.points());
                weekly.setStreak(record.streak());
                task = weekly;
            } else {
                task = new OneTimeTask(record.name(), record.description(), record.points());
            }
            task.setCompleted(record.completed());
            tasks.add(task);
        }
        userService.getUser().applyImportedState(
                payload.username(),
                payload.level(),
                payload.experience(),
                payload.gold(),
                payload.health(),
                payload.title(),
                tasks,
                payload.completionsByDate(),
                payload.xpHistory()
        );
    }

    /**
     * Verschlüsselt einen Klartext mit AES/GCM und einem aus dem Passwort
     * abgeleiteten Schlüssel.
     *
     * @param plaintext der zu verschlüsselnde Text
     * @param password  das Passwort zur Schlüsselableitung
     * @return das verschlüsselte Paket (Salt, IV und Geheimtext)
     * @throws Exception bei einem Fehler der Verschlüsselung
     */
    private EncryptedPayload encrypt(String plaintext, String password) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);
        SecretKeySpec key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return new EncryptedPayload(
                "AES/GCM",
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(encrypted)
        );
    }

    /**
     * Entschlüsselt ein zuvor verschlüsseltes Paket.
     *
     * @param payload  das verschlüsselte Paket
     * @param password das Passwort zur Schlüsselableitung
     * @return der entschlüsselte Klartext
     * @throws Exception bei einem Fehler der Entschlüsselung
     */
    private String decrypt(EncryptedPayload payload, String password) throws Exception {
        byte[] salt = Base64.getDecoder().decode(payload.saltBase64());
        byte[] iv = Base64.getDecoder().decode(payload.ivBase64());
        byte[] encrypted = Base64.getDecoder().decode(payload.cipherTextBase64());
        SecretKeySpec key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] plain = cipher.doFinal(encrypted);
        return new String(plain, StandardCharsets.UTF_8);
    }

    /**
     * Leitet aus Passwort und Salt mittels PBKDF2 einen AES-Schlüssel ab.
     *
     * @param password das Passwort
     * @param salt     das Salt
     * @return der abgeleitete Schlüssel
     * @throws Exception bei einem Fehler der Schlüsselableitung
     */
    private SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Ermittelt das Passwort für die Sicherungs-Verschlüsselung aus der
     * Umgebungsvariablen {@code DISCIPLICA_BACKUP_KEY} oder weicht auf einen
     * Entwicklungsschlüssel aus.
     *
     * @return das zu verwendende Passwort
     */
    private String resolveEncryptionPassword() {
        String env = System.getenv("DISCIPLICA_BACKUP_KEY");
        if (env != null && !env.isBlank()) {
            return env;
        }
        logger.warn("DISCIPLICA_BACKUP_KEY not set. Falling back to development key.");
        return "disciplica-dev-key-change-me";
    }

    /**
     * Bereitet einen Wert für die CSV-Ausgabe auf (in Anführungszeichen,
     * Anführungszeichen verdoppelt).
     *
     * @param value der Ausgangswert
     * @return der CSV-sichere Wert
     */
    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    /**
     * Ein einzelner Aufgaben-Datensatz innerhalb einer Sicherung.
     *
     * @param type        die Art der Aufgabe
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Punktewert
     * @param completed   der Erledigt-Status
     * @param streak      die Serie
     */
    public record TaskRecord(String type, String name, String description, int points, boolean completed, int streak) {
    }

    /**
     * Das gesamte Datenpaket einer Sicherung.
     *
     * @param schemaVersion     die Version des Sicherungsformats
     * @param exportedAt        der Zeitpunkt des Exports
     * @param username          der Benutzername
     * @param level             das Level
     * @param experience        die Erfahrungspunkte
     * @param gold              das Gold
     * @param health            die Lebenspunkte
     * @param title             der Titel
     * @param tasks             die Aufgaben
     * @param completionsByDate die Erledigungen je Tag
     * @param xpHistory         der XP-Verlauf
     */
    public record BackupPayload(String schemaVersion,
                                String exportedAt,
                                String username,
                                int level,
                                int experience,
                                int gold,
                                int health,
                                String title,
                                List<TaskRecord> tasks,
                                Map<String, Integer> completionsByDate,
                                List<Integer> xpHistory) {
    }

    /**
     * Ein verschlüsseltes Sicherungspaket.
     *
     * @param algorithm        der verwendete Algorithmus
     * @param saltBase64        das Salt (Base64-kodiert)
     * @param ivBase64          der Initialisierungsvektor (Base64-kodiert)
     * @param cipherTextBase64  der verschlüsselte Inhalt (Base64-kodiert)
     */
    public record EncryptedPayload(String algorithm, String saltBase64, String ivBase64, String cipherTextBase64) {
    }
}
