package com.disciplica.shared.task;

import jakarta.validation.constraints.Size;

/**
 * Anfrage zum Aktualisieren einer bestehenden Aufgabe. Nicht gesetzte
 * Felder ({@code null}) bleiben unverändert.
 *
 * @param title       neuer Titel oder {@code null}, wenn unverändert
 * @param description neue Beschreibung oder {@code null}, wenn unverändert
 * @param points      neuer Punktewert oder {@code null}, wenn unverändert
 * @param completed   neuer Erledigt-Status oder {@code null}, wenn unverändert
 * @param category    neue Kategorie oder {@code null}, wenn unverändert
 */
public record UpdateTaskRequest(
        @Size(max = 120) String title,
        @Size(max = 1000) String description,
        Integer points,
        Boolean completed,
        String category
) {
}
