package model.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Berechnet aus gesammelten Erfahrungspunkten das Level eines Benutzers.
 * <p>
 * Bis zu einer festgelegten Liste von Schwellenwerten gelten diese direkt;
 * darüber hinaus wachsen die benötigten Punkte exponentiell weiter.
 */
public class LevelCalculator {
    private final List<Integer> thresholds;
    private final double exponentialGrowthFactor;

    /**
     * Erzeugt einen Rechner mit Standardwerten (Schwellen 100/250/500/1000,
     * Wachstumsfaktor 2,0).
     */
    public LevelCalculator() {
        this(List.of(100, 250, 500, 1000), 2.0);
    }

    /**
     * Erzeugt einen Rechner mit eigenen Schwellenwerten und Wachstumsfaktor.
     *
     * @param thresholds              die festen Schwellenwerte (mindestens
     *                                einer)
     * @param exponentialGrowthFactor der Faktor für das weitere Wachstum
     *                                (muss größer als 1,0 sein)
     * @throws IllegalArgumentException wenn die Schwellen leer sind oder der
     *                                  Faktor nicht größer als 1,0 ist
     */
    public LevelCalculator(List<Integer> thresholds, double exponentialGrowthFactor) {
        if (thresholds == null || thresholds.isEmpty()) {
            throw new IllegalArgumentException("Thresholds must not be empty");
        }
        if (exponentialGrowthFactor <= 1.0) {
            throw new IllegalArgumentException("Exponential growth factor must be > 1.0");
        }
        this.thresholds = Collections.unmodifiableList(new ArrayList<>(thresholds));
        this.exponentialGrowthFactor = exponentialGrowthFactor;
    }

    /**
     * Gibt die für ein bestimmtes Level benötigten Erfahrungspunkte zurück.
     *
     * @param level das Level
     * @return die benötigten Punkte für dieses Level
     */
    public int thresholdForLevel(int level) {
        if (level <= 1) {
            return thresholds.get(0);
        }
        int index = level - 2;
        if (index < thresholds.size()) {
            return thresholds.get(index);
        }
        int threshold = thresholds.get(thresholds.size() - 1);
        for (int i = thresholds.size(); i <= index; i++) {
            threshold = (int) Math.round(threshold * exponentialGrowthFactor);
        }
        return threshold;
    }

    /**
     * Berechnet das Level, das einer Gesamtzahl an Erfahrungspunkten
     * entspricht.
     *
     * @param totalXp die gesamten Erfahrungspunkte
     * @return das erreichte Level
     * @throws IllegalArgumentException wenn die Punkte negativ sind
     */
    public int calculateLevel(int totalXp) {
        if (totalXp < 0) {
            throw new IllegalArgumentException("Total XP must be non-negative");
        }
        int level = 1;
        int remainingXp = totalXp;
        while (remainingXp >= thresholdForLevel(level)) {
            remainingXp -= thresholdForLevel(level);
            level++;
        }
        return level;
    }

    /**
     * Berechnet, wie viele Erfahrungspunkte bereits in das aktuelle Level
     * geflossen sind.
     *
     * @param totalXp die gesamten Erfahrungspunkte
     * @return die Punkte innerhalb des aktuellen Levels
     * @throws IllegalArgumentException wenn die Punkte negativ sind
     */
    public int xpIntoCurrentLevel(int totalXp) {
        if (totalXp < 0) {
            throw new IllegalArgumentException("Total XP must be non-negative");
        }
        int level = 1;
        int remainingXp = totalXp;
        while (remainingXp >= thresholdForLevel(level)) {
            remainingXp -= thresholdForLevel(level);
            level++;
        }
        return remainingXp;
    }

    /**
     * Berechnet, wie viele Erfahrungspunkte bis zum nächsten Level fehlen.
     *
     * @param totalXp die gesamten Erfahrungspunkte
     * @return die bis zum nächsten Level fehlenden Punkte
     */
    public int xpToNextLevel(int totalXp) {
        int level = calculateLevel(totalXp);
        int current = xpIntoCurrentLevel(totalXp);
        return thresholdForLevel(level) - current;
    }
}
