package com.disciplica;

import com.disciplica.testtags.UnitTest;
import model.service.GamificationEngine;
import model.service.LevelCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class GamificationEngineTest {

    @Test
    @DisplayName("XP follows difficulty x quality multiplier x streak bonus")
    void calculatesRewardsUsingFormula() {
        GamificationEngine engine = new GamificationEngine();
        GamificationEngine.RewardResult result = engine.calculateRewards("hard", 1, 5, 0);

        assertEquals(59, result.xpAward());
        assertTrue(result.goldAward() > 0);
        assertEquals(1, result.oldLevel());
        assertEquals(1, result.newLevel());
    }

    @Test
    @DisplayName("Calculates level-up when reward crosses threshold")
    void calculatesLevelUpFromTotalXp() {
        GamificationEngine engine = new GamificationEngine(
                GamificationEngine.GamificationRules.defaultRules(),
                new LevelCalculator()
        );
        GamificationEngine.RewardResult result = engine.calculateRewards("medium", 1, 10, 90);

        assertEquals(2, result.newLevel());
        assertTrue(result.xpIntoLevel() >= 0);
    }

    @Test
    @DisplayName("Missed habit applies HP penalty by difficulty")
    void appliesHealthPenalty() {
        GamificationEngine engine = new GamificationEngine();
        GamificationEngine.PenaltyResult penalty = engine.calculateMissPenalty("hard", 40);

        assertEquals(8, penalty.hpLoss());
        assertEquals(32, penalty.resultingHealth());
    }

    @Test
    @DisplayName("Gold reward cost can be calculated for custom rewards")
    void computesRewardCost() {
        GamificationEngine engine = new GamificationEngine();
        int goldCost = engine.rewardCostInGold(50);
        assertEquals(10, goldCost);
    }
}
