package com.disciplica.shared.task;

/**
 * Tagesweise zusammengefasste Aktivität eines Benutzers für das Dashboard.
 *
 * @param date        der Tag im Format {@code YYYY-MM-DD} (UTC)
 * @param completions Anzahl der an diesem Tag abgeschlossenen Aufgaben
 * @param xpEarned    an diesem Tag gesammelte Erfahrungspunkte
 */
public record DailyActivityDto(
        String date,
        int completions,
        int xpEarned
) {
}
