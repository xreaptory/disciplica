package model.bootstrap;

import javafx.application.Application;

/**
 * Startet die JavaFX-Anwendung, indem die eigentliche Anwendungsklasse
 * {@link HabitTrackerApp} hochgefahren wird.
 */
public class Main {

    /**
     * Startet die JavaFX-Laufzeit und damit die Anwendung.
     *
     * @param args die Kommandozeilenargumente
     */
    public static void main(String[] args) {
        Application.launch(HabitTrackerApp.class, args);
    }
}
