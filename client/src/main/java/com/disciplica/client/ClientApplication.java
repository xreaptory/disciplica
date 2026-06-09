package com.disciplica.client;

/**
 * Start-Klasse der ausführbaren Anwendung (Einstiegspunkt der lauffähigen
 * JAR).
 * <p>
 * Diese Klasse erweitert bewusst <em>nicht</em> {@code javafx.application.
 * Application}, sondern reicht den Start nur an {@link model.bootstrap.Main}
 * weiter. Dadurch lässt sich die Anwendung als gewöhnliche
 * {@code java -jar}-Datei starten, ohne dass die JavaFX-Laufzeit gesondert
 * eingebunden werden muss.
 */
public final class ClientApplication {

    private ClientApplication() {
    }

    /**
     * Einstiegspunkt der ausführbaren JAR.
     *
     * @param args die Kommandozeilenargumente
     */
    public static void main(String[] args) {
        model.bootstrap.Main.main(args);
    }
}
