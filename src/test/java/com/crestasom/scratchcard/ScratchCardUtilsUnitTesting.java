package com.crestasom.scratchcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.crestasom.scratchcard.config.BonusSymbolProbability;
import com.crestasom.scratchcard.config.GameConfig;
import com.crestasom.scratchcard.config.ProbabilitiesConfig;
import com.crestasom.scratchcard.config.StandardSymbolProbability;
import com.crestasom.scratchcard.config.SymbolProbability;
import com.crestasom.scratchcard.entity.CurrentMatrix;
import com.crestasom.scratchcard.util.ScratchCardUtils;

public class ScratchCardUtilsUnitTesting {

    @AfterEach
    void resetStaticFields() {
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "maxBonusSymbol", 3);
    }
    @Test
    void testInitCurrentSymbol_SelectsCorrectSymbol() {
        // Arrange
        SymbolProbability symbolProbability = new StandardSymbolProbability();
        Map<String, Integer> symbols = new LinkedHashMap<>();
        symbols.put("A", 2); // Weight 2
        symbols.put("B", 3); // Weight 3
        symbols.put("C", 5); // Weight 5
        symbolProbability.setSymbols(symbols);

        CurrentMatrix matrix = new CurrentMatrix(1, 1); // 1x1 matrix
        GameConfig config = new GameConfig();

        int randValue = 6; // This should select "C" (2+3 = 5, so 6 lands in C range)

        StaticTestUtils.setStaticField(ScratchCardUtils.class, "random", new RandomStub(randValue));

        // Act
        ScratchCardUtils.initCurrentSymbol(symbolProbability, matrix, config, 0, 0);

        // Assert
        assertEquals("C", matrix.getValue(0, 0));
    }

    @Test
    void testInitCurrentSymbol_ThrowsIfSymbolNotFound() {
        SymbolProbability symbolProbability = new StandardSymbolProbability();
        symbolProbability.setSymbols(Collections.emptyMap());

        CurrentMatrix matrix = new CurrentMatrix(1, 1);
        GameConfig config = new GameConfig();

        assertThrows(RuntimeException.class,
                () -> ScratchCardUtils.initCurrentSymbol(symbolProbability, matrix, config, 0, 0));
    }

    @Test
    void testPickBonusPositions_ReturnsUniquePositions() {
        int rows = 3;
        int cols = 3;
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "random", new RandomStub(3)); // deterministic shuffle

        List<int[]> result = ScratchCardUtils.pickBonusPositions(rows, cols);

        assertEquals(3, result.size());
        Set<String> seen = new HashSet<>();
        for (int[] pos : result) {
            assertTrue(pos[0] < rows);
            assertTrue(pos[1] < cols);
            assertTrue(seen.add(pos[0] + "," + pos[1]));
        }
    }

    @Test
    void testPickBonusPositions_TooManyBonusSymbols_Throws() {
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "maxBonusSymbol", 10);
        assertThrows(IllegalArgumentException.class, () -> ScratchCardUtils.pickBonusPositions(2, 2));
    }

    @Test
    void testAddBonusSymbol_PopulatesBonusPositions() {
        // Setup 3x3 grid
        int rows = 3, cols = 3;
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "rowNum", rows);
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "colNum", cols);
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "maxBonusSymbol", 2);
        StaticTestUtils.setStaticField(ScratchCardUtils.class, "random", new RandomStub(2));
        List<StandardSymbolProbability> standardSymbols = new ArrayList<>();
        ProbabilitiesConfig probConfig = new ProbabilitiesConfig();


        GameConfig gameConfig = new GameConfig();
        gameConfig.setProbabilities(probConfig);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StandardSymbolProbability symbolProb = new StandardSymbolProbability();
                symbolProb.setRow(r);
                symbolProb.setColumn(c);
                Map<String, Integer> symbols = new LinkedHashMap<>();
                symbols.put("A", 1); // simple dummy data
                symbolProb.setSymbols(symbols);
                standardSymbols.add(symbolProb);
            }
        }
        probConfig.setStandardSymbols(standardSymbols);
        // Symbol setup
        BonusSymbolProbability bonusProb = new BonusSymbolProbability();
        Map<String, Integer> bonuses = new LinkedHashMap<>();
        bonuses.put("BONUS", 10);
        bonuses.put("BONUS1", 1);
        bonusProb.setSymbols(bonuses);
        probConfig.setBonusSymbols(bonusProb);

        CurrentMatrix matrix = new CurrentMatrix(rows, cols);
        System.out.println("here");
        // Act
        ScratchCardUtils.addBonusSymbol(gameConfig, matrix);
        System.out.println("end");
        // Assert: check that exactly two cells are set to BONUS
        int bonusCount = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if ("BONUS".equals(matrix.getValue(r, c))) {
                    bonusCount++;
                }
            }
        }
        assertEquals(2, bonusCount);
    }
}


class StaticTestUtils {

    public static void setStaticField(Class<?> clazz, String fieldName, Object newValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            // Remove final modifier if present
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, newValue); // null for static field
        } catch (Exception e) {
            throw new RuntimeException("Failed to set static field: " + fieldName, e);
        }
    }

    public static Object getStaticField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get static field: " + fieldName, e);
        }
    }
}


class RandomStub extends Random {
    private static final long serialVersionUID = 8290299943454578256L;
    private final int fixedValue;

    public RandomStub(int value) {
        this.fixedValue = value;
    }

    @Override
    public int nextInt(int bound) {
        return fixedValue;
    }
}
