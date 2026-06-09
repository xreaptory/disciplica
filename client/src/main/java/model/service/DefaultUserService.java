package model.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import model.domain.model.User;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Standard-Umsetzung von {@link UserService}.
 * <p>
 * Arbeitet auf einem festen Benutzer und delegiert das Laden/Speichern an
 * dessen Methoden sowie Sicherungen und Exporte an den
 * {@link DataPortabilityService}.
 */
@Singleton
public class DefaultUserService implements UserService {
    private final User user;
    private final DataPortabilityService dataPortabilityService;

    /**
     * Erzeugt den Dienst mit dem Benutzer und dem Dienst für Datenexport und
     * Sicherungen.
     *
     * @param user                   der verwaltete Benutzer
     * @param dataPortabilityService der Dienst für Export und Sicherung
     */
    @Inject
    public DefaultUserService(User user, DataPortabilityService dataPortabilityService) {
        this.user = user;
        this.dataPortabilityService = dataPortabilityService;
    }

    /**
     * {@return der verwaltete Benutzer}
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * Lädt die Aufgaben des Benutzers aus der Datei.
     *
     * @throws IOException bei einem Lesefehler
     */
    @Override
    public void readTaskData() throws IOException {
        user.readTaskTxt();
    }

    /**
     * Speichert die Aufgaben des Benutzers in die Datei.
     *
     * @throws IOException bei einem Schreibfehler
     */
    @Override
    public void writeTaskData() throws IOException {
        user.writeTaskTxt();
    }

    /**
     * Lädt die Benutzerdaten aus der Datei.
     *
     * @throws IOException bei einem Lesefehler
     */
    @Override
    public void readUserData() throws IOException {
        user.readUserTxt();
    }

    /**
     * Speichert die Benutzerdaten in die Datei.
     *
     * @throws IOException bei einem Schreibfehler
     */
    @Override
    public void writeUserData() throws IOException {
        user.writeUserTxt();
    }

    /**
     * Exportiert eine verschlüsselte JSON-Sicherung.
     *
     * @param outputFile die Zieldatei
     * @return der Pfad der erstellten Sicherung
     */
    @Override
    public Path exportEncryptedJsonBackup(Path outputFile) {
        return dataPortabilityService.exportEncryptedJson(outputFile);
    }

    /**
     * Importiert eine verschlüsselte JSON-Sicherung.
     *
     * @param inputFile die Sicherungsdatei
     */
    @Override
    public void importEncryptedJsonBackup(Path inputFile) {
        dataPortabilityService.importEncryptedJson(inputFile);
    }

    /**
     * Exportiert die Gewohnheiten als CSV-Datei.
     *
     * @param outputFile die Zieldatei
     * @return der Pfad der erstellten CSV-Datei
     */
    @Override
    public Path exportHabitsCsv(Path outputFile) {
        return dataPortabilityService.exportCsv(outputFile);
    }

    /**
     * Bereitet den Ordner für die Cloud-Synchronisierung vor.
     *
     * @return der Pfad des vorbereiteten Ordners
     */
    @Override
    public Path prepareCloudSyncFolder() {
        return dataPortabilityService.ensureCloudSyncPreparationPath();
    }
}
