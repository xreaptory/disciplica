package model.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelCalculator {
    private final List<Integer> thresholds;
    private final double exponentialGrowthFactor;

    public LevelCalculator() {
        this(List.of(100, 250, 500, 1000), 2.0);
    }

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

    public int xpToNextLevel(int totalXp) {
        int level = calculateLevel(totalXp);
        int current = xpIntoCurrentLevel(totalXp);
        return thresholdForLevel(level) - current;
    }
}
