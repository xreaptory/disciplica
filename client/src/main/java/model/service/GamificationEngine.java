package model.service;

import java.util.Map;

public class GamificationEngine {
    private final GamificationRules rules;
    private final LevelCalculator levelCalculator;

    public GamificationEngine() {
        this(GamificationRules.defaultRules(), new LevelCalculator());
    }

    public GamificationEngine(GamificationRules rules, LevelCalculator levelCalculator) {
        if (rules == null) {
            throw new IllegalArgumentException("Rules must not be null");
        }
        if (levelCalculator == null) {
            throw new IllegalArgumentException("LevelCalculator must not be null");
        }
        this.rules = rules;
        this.levelCalculator = levelCalculator;
    }

    public RewardResult calculateRewards(String difficulty, int quality, int streak, int currentTotalXp) {
        double difficultyWeight = rules.difficultyWeights().getOrDefault(difficulty, rules.defaultDifficultyWeight());
        double qualityMultiplier = rules.qualityMultipliers().getOrDefault(quality, rules.defaultQualityMultiplier());
        double streakBonusMultiplier = 1.0 + (Math.max(0, streak) * rules.streakBonusPerStreak());

        int xpAward = Math.max(0, (int) Math.round(difficultyWeight * qualityMultiplier * streakBonusMultiplier));
        int goldAward = Math.max(0, (int) Math.round(rules.baseGoldReward() + (xpAward * rules.goldPerXpFactor())));

        int newTotalXp = Math.max(0, currentTotalXp) + xpAward;
        int oldLevel = levelCalculator.calculateLevel(Math.max(0, currentTotalXp));
        int newLevel = levelCalculator.calculateLevel(newTotalXp);

        return new RewardResult(
                xpAward,
                goldAward,
                0,
                oldLevel,
                newLevel,
                levelCalculator.xpIntoCurrentLevel(newTotalXp),
                levelCalculator.thresholdForLevel(newLevel)
        );
    }

    public PenaltyResult calculateMissPenalty(String difficulty, int currentHealth) {
        int hpLoss = rules.healthPenaltyByDifficulty().getOrDefault(difficulty, rules.defaultHealthPenalty());
        int newHealth = Math.max(0, currentHealth - hpLoss);
        return new PenaltyResult(hpLoss, newHealth);
    }

    public int rewardCostInGold(int rewardPointCost) {
        if (rewardPointCost < 0) {
            throw new IllegalArgumentException("Reward point cost must be non-negative");
        }
        return Math.max(1, (int) Math.round(rewardPointCost * rules.goldCostPerRewardPoint()));
    }

    public record RewardResult(
            int xpAward,
            int goldAward,
            int hpChange,
            int oldLevel,
            int newLevel,
            int xpIntoLevel,
            int xpThresholdForLevel) {
    }

    public record PenaltyResult(int hpLoss, int resultingHealth) {
    }

    public record GamificationRules(
            Map<String, Double> difficultyWeights,
            Map<Integer, Double> qualityMultipliers,
            Map<String, Integer> healthPenaltyByDifficulty,
            double streakBonusPerStreak,
            double defaultDifficultyWeight,
            double defaultQualityMultiplier,
            int defaultHealthPenalty,
            int baseGoldReward,
            double goldPerXpFactor,
            double goldCostPerRewardPoint) {

        public static GamificationRules defaultRules() {
            return new GamificationRules(
                    Map.of(
                            "easy", 10.0,
                            "medium", 18.0,
                            "hard", 28.0
                    ),
                    Map.of(
                            -1, 0.6,
                            0, 1.0,
                            1, 1.4
                    ),
                    Map.of(
                            "easy", 3,
                            "medium", 5,
                            "hard", 8
                    ),
                    0.08,
                    15.0,
                    1.0,
                    4,
                    2,
                    0.15,
                    0.2
            );
        }
    }
}
