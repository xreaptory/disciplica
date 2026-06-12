package com.disciplica.server.user;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Berechnet aus gesammelten Erfahrungspunkten das Level eines Benutzers.
 * <p>
 * Die Formel spiegelt bewusst den clientseitigen {@code LevelCalculator}
 * (Schwellen 100/250/500/1000, danach Wachstumsfaktor 2,0), damit Server und
 * Client dasselbe Level ergeben. Sollte die Formel später vereinheitlicht
 * werden, gehört sie ins {@code shared}-Modul.
 */
@Component
public class LevelCalculator {
    private static final List<Integer> THRESHOLDS = List.of(100, 250, 500, 1000);
    private static final double GROWTH_FACTOR = 2.0;

    /**
     * Gibt die für ein bestimmtes Level zusätzlich benötigten Erfahrungspunkte
     * zurück.
     *
     * @param level das Level
     * @return die für dieses Level benötigten Punkte
     */
    private int thresholdForLevel(int level) {
        if (level <= 1) {
            return THRESHOLDS.get(0);
        }
        int index = level - 2;
        if (index < THRESHOLDS.size()) {
            return THRESHOLDS.get(index);
        }
        int threshold = THRESHOLDS.get(THRESHOLDS.size() - 1);
        for (int i = THRESHOLDS.size(); i <= index; i++) {
            threshold = (int) Math.round(threshold * GROWTH_FACTOR);
        }
        return threshold;
    }

    /**
     * Berechnet das Level, das einer Gesamtzahl an Erfahrungspunkten entspricht.
     *
     * @param totalXp die gesamten Erfahrungspunkte (negative Werte gelten als 0)
     * @return das erreichte Level (mindestens 1)
     */
    public int calculateLevel(int totalXp) {
        int remainingXp = Math.max(0, totalXp);
        int level = 1;
        while (remainingXp >= thresholdForLevel(level)) {
            remainingXp -= thresholdForLevel(level);
            level++;
        }
        return level;
    }
}
