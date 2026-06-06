package com.disciplica;

import com.disciplica.testtags.UnitTest;
import model.service.LevelCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
class LevelCalculatorTest {

    @Test
    @DisplayName("Uses exponential thresholds 100, 250, 500, 1000 by default")
    void defaultThresholdsAreExponential() {
        LevelCalculator calculator = new LevelCalculator();
        assertEquals(100, calculator.thresholdForLevel(1));
        assertEquals(250, calculator.thresholdForLevel(2));
        assertEquals(500, calculator.thresholdForLevel(3));
        assertEquals(1000, calculator.thresholdForLevel(4));
        assertEquals(2000, calculator.thresholdForLevel(5));
    }

    @Test
    @DisplayName("Calculates level and progress from total XP")
    void calculatesLevelAndProgress() {
        LevelCalculator calculator = new LevelCalculator(List.of(100, 250, 500, 1000), 2.0);
        assertEquals(1, calculator.calculateLevel(99));
        assertEquals(2, calculator.calculateLevel(100));
        assertEquals(3, calculator.calculateLevel(350));
        assertEquals(4, calculator.calculateLevel(850));
        assertEquals(50, calculator.xpIntoCurrentLevel(900));
        assertEquals(950, calculator.xpToNextLevel(900));
    }
}
