package com.disciplica.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Server-Anwendung (Spring Boot).
 * <p>
 * Startet den eingebetteten Webserver und initialisiert alle
 * Spring-Komponenten (Controller, Services, Repositories).
 */
@SpringBootApplication
public class DisciplicaServerApplication {

    /**
     * Startet die Server-Anwendung.
     *
     * @param args Kommandozeilenargumente, die an Spring Boot weitergereicht
     *             werden
     */
    public static void main(String[] args) {
        SpringApplication.run(DisciplicaServerApplication.class, args);
    }
}
