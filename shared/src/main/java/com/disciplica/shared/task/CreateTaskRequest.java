package com.disciplica.shared.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Anfrage zum Anlegen einer neuen Aufgabe (Gewohnheit, Daily, To-do oder
 * Belohnung).
 *
 * @param type        Art der Aufgabe
 * @param title       Titel der Aufgabe (höchstens 120 Zeichen)
 * @param description optionale Beschreibung (höchstens 1000 Zeichen)
 * @param points      Punktewert, der beim Erledigen vergeben wird
 * @param category    optionale Kategorie zur Gruppierung
 */
public record CreateTaskRequest(
        @NotNull TaskType type,
        @NotBlank @Size(max = 120) String title,
        @Size(max = 1000) String description,
        int points,
        String category
) {
}
