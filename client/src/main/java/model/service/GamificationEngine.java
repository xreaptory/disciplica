package model.service;

import java.util.Map;

/**
 * Berechnet die Spielmechanik (Gamification): Belohnungen für erledigte
 * Aufgaben, Strafen für Versäumnisse und die Goldkosten von Belohnungen.
 * <p>
 * Die konkreten Werte stammen aus einem Satz von {@link GamificationRules},
 * die Level-Berechnung übernimmt ein {@link LevelCalculator}.
 */
public class GamificationEngine {
    private final GamificationRules rules;
    private final LevelCalculator levelCalculator;

    /**
     * Erzeugt die Engine mit den Standardregeln und einem
     * Standard-{@link LevelCalculator}.
     */
    public GamificationEngine() {
        this(GamificationRules.defaultRules(), new LevelCalculator());
    }

    /**
     * Erzeugt die Engine mit eigenen Regeln und Level-Rechner.
     *
     * @param rules           die Spielregeln
     * @param levelCalculator der Level-Rechner
     * @throws IllegalArgumentException wenn ein Wert {@code null} ist
     */
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

    /**
     * Berechnet die Belohnung (Erfahrung und Gold) für das Erledigen einer
     * Aufgabe und ermittelt einen etwaigen Stufenaufstieg.
     *
     * @param difficulty     der Schwierigkeitsgrad der Aufgabe
     * @param quality        die Qualität der Erfüllung
     * @param streak         die aktuelle Serie
     * @param currentTotalXp die bisherigen Gesamt-Erfahrungspunkte
     * @return das Ergebnis mit verdienter Erfahrung, Gold und Levelangaben
     */
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

    /**
     * Berechnet den Lebenspunkteverlust für eine versäumte Aufgabe.
     *
     * @param difficulty    der Schwierigkeitsgrad der Aufgabe
     * @param currentHealth die aktuellen Lebenspunkte
     * @return das Ergebnis mit Verlust und verbleibenden Lebenspunkten
     */
    public PenaltyResult calculateMissPenalty(String difficulty, int currentHealth) {
        int hpLoss = rules.healthPenaltyByDifficulty().getOrDefault(difficulty, rules.defaultHealthPenalty());
        int newHealth = Math.max(0, currentHealth - hpLoss);
        return new PenaltyResult(hpLoss, newHealth);
    }

    /**
     * Berechnet die Goldkosten einer Belohnung anhand ihres Punktewerts.
     *
     * @param rewardPointCost der Punktewert der Belohnung
     * @return die Kosten in Gold (mindestens 1)
     * @throws IllegalArgumentException wenn der Punktewert negativ ist
     */
    public int rewardCostInGold(int rewardPointCost) {
        if (rewardPointCost < 0) {
            throw new IllegalArgumentException("Reward point cost must be non-negative");
        }
        return Math.max(1, (int) Math.round(rewardPointCost * rules.goldCostPerRewardPoint()));
    }

    /**
     * Ergebnis einer Belohnungsberechnung.
     *
     * @param xpAward             die verdienten Erfahrungspunkte
     * @param goldAward           das verdiente Gold
     * @param hpChange            die Änderung der Lebenspunkte
     * @param oldLevel            das Level vor der Belohnung
     * @param newLevel            das Level nach der Belohnung
     * @param xpIntoLevel         die in das neue Level geflossenen Punkte
     * @param xpThresholdForLevel die für das neue Level benötigten Punkte
     */
    public record RewardResult(
            int xpAward,
            int goldAward,
            int hpChange,
            int oldLevel,
            int newLevel,
            int xpIntoLevel,
            int xpThresholdForLevel) {
    }

    /**
     * Ergebnis einer Strafberechnung.
     *
     * @param hpLoss          die verlorenen Lebenspunkte
     * @param resultingHealth die verbleibenden Lebenspunkte
     */
    public record PenaltyResult(int hpLoss, int resultingHealth) {
    }

    /**
     * Regelsatz für die Spielmechanik.
     *
     * @param difficultyWeights        Gewichtung je Schwierigkeitsgrad
     * @param qualityMultipliers       Multiplikator je Qualitätsstufe
     * @param healthPenaltyByDifficulty Lebenspunkteverlust je Schwierigkeitsgrad
     * @param streakBonusPerStreak     Bonus je Serienschritt
     * @param defaultDifficultyWeight  Standardgewichtung, wenn der Grad
     *                                 unbekannt ist
     * @param defaultQualityMultiplier Standardmultiplikator, wenn die Qualität
     *                                 unbekannt ist
     * @param defaultHealthPenalty     Standardverlust, wenn der Grad unbekannt
     *                                 ist
     * @param baseGoldReward           Grundbetrag an Gold je Belohnung
     * @param goldPerXpFactor          zusätzlicher Goldanteil je
     *                                 Erfahrungspunkt
     * @param goldCostPerRewardPoint   Goldkosten je Belohnungspunkt
     */
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

        /**
         * {@return ein Regelsatz mit ausgewogenen Standardwerten}
         */
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
