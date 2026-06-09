package com.disciplica.shared.task;

import java.time.Instant;
import java.util.UUID;

/**
 * Vollständige Beschreibung einer Aufgabe, wie sie zwischen Server und
 * Client ausgetauscht wird.
 *
 * @param id          eindeutige Kennung der Aufgabe
 * @param type        Art der Aufgabe
 * @param title       Titel der Aufgabe
 * @param description Beschreibung der Aufgabe
 * @param points      Punktewert der Aufgabe
 * @param streak      aktuelle Serie aufeinanderfolgender Erledigungen
 * @param completed   {@code true}, wenn die Aufgabe als erledigt markiert ist
 * @param category    Kategorie der Aufgabe
 * @param createdAt   Zeitpunkt der Erstellung
 * @param updatedAt   Zeitpunkt der letzten Änderung
 */
public record TaskDto(
        UUID id,
        TaskType type,
        String title,
        String description,
        int points,
        int streak,
        boolean completed,
        String category,
        Instant createdAt,
        Instant updatedAt
) {
}
