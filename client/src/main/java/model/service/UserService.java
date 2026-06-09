package model.service;

import model.domain.model.User;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Schnittstelle für alle Dienste rund um den Benutzer: Zugriff auf den
 * aktuellen Benutzer, Laden und Speichern der Daten sowie Sicherungen und
 * Datenexport.
 */
public interface UserService {

    /**
     * {@return der aktuelle Benutzer}
     */
    User getUser();

    /**
     * Lädt die Aufgaben des Benutzers.
     *
     * @throws IOException bei einem Lesefehler
     */
    void readTaskData() throws IOException;

    /**
     * Speichert die Aufgaben des Benutzers.
     *
     * @throws IOException bei einem Schreibfehler
     */
    void writeTaskData() throws IOException;

    /**
     * Lädt die Benutzerdaten (Werte und Verlauf).
     *
     * @throws IOException bei einem Lesefehler
     */
    void readUserData() throws IOException;

    /**
     * Speichert die Benutzerdaten (Werte und Verlauf).
     *
     * @throws IOException bei einem Schreibfehler
     */
    void writeUserData() throws IOException;

    /**
     * Exportiert eine verschlüsselte JSON-Sicherung der Benutzerdaten.
     *
     * @param outputFile die Zieldatei
     * @return der Pfad der erstellten Sicherung
     */
    Path exportEncryptedJsonBackup(Path outputFile);

    /**
     * Importiert eine zuvor erstellte verschlüsselte JSON-Sicherung.
     *
     * @param inputFile die Sicherungsdatei
     */
    void importEncryptedJsonBackup(Path inputFile);

    /**
     * Exportiert die Gewohnheiten als CSV-Datei.
     *
     * @param outputFile die Zieldatei
     * @return der Pfad der erstellten CSV-Datei
     */
    Path exportHabitsCsv(Path outputFile);

    /**
     * Bereitet einen Ordner für die Cloud-Synchronisierung vor.
     *
     * @return der Pfad des vorbereiteten Ordners
     */
    Path prepareCloudSyncFolder();
}
