package com.crestasom.scratchcard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.crestasom.scratchcard.config.GameConfig;
import com.crestasom.scratchcard.entity.GameState;
import com.crestasom.scratchcard.util.ScratchCardUtils;

public class ScratchCardUtilsTest {

    static GameConfig config;

    @BeforeAll
    static void setup() throws Exception {
        config = ScratchCardUtils
                .loadConfig("config.json");
    }

    private GameState matrixWith(String[][] values, long bet, List<String> bonusSymbols) {
        GameState matrix = new GameState(3, 3);
        matrix.setMatrix(values);
        matrix.setBetAmount(bet);
        if (bonusSymbols != null) {
            matrix.setBonusSymbols(bonusSymbols);
        }
        for (int r = 0; r < values.length; r++) {
            for (int c = 0; c < values[r].length; c++) {
                String symbol = values[r][c];
                matrix.incrementSymbolCount(symbol);
            }
        }
        return matrix;
    }

    @Test
    void testSameSymbol3Times() {
        String[][] values = {{"A", "A", "A"}, {"B", "C", "D"}, {"E", "F", "B"}};
        GameState matrix = matrixWith(values, 100, null);
        ScratchCardUtils.calculate(config, matrix);
        long expectedReward = 100 * 1 * 2;// A appears 3 times(1x) and horizontal(2x)
        assertEquals(expectedReward, matrix.getReward());
    }

    @Test
    void testSameSymbolHorizontally() {
        String[][] values = {{"F", "F", "F"}, {"A", "B", "C"}, {"D", "E", "B"}};
        GameState matrix = matrixWith(values, 100, null);
        ScratchCardUtils.calculate(config, matrix);
        long expectedReward = 100 * 2;// F appears 3 times(1x) and vertical(2x)
        assertEquals(expectedReward, matrix.getReward());
    }

    @Test
    void testSameSymbolVertically() {
        String[][] values = {{"C", "A", "F"}, {"C", "B", "F"}, {"C", "E", "F"}};
        GameState matrix = matrixWith(values, 100, null);
        ScratchCardUtils.calculate(config, matrix);
        long expectedReward = 100 * 2 + 100 * 2;// C appears 3 times(1x) and vertical(2x) and f appears 3 times(1x) and
                                      // vertical(2x)
        assertEquals(expectedReward, matrix.getReward()); // C(2.5x) * 100
    }

    @Test
    void testDiagonalLeftToRight() {
        String[][] values = {{"E", "B", "C"}, {"D", "E", "F"}, {"A", "C", "E"}};
        GameState matrix = matrixWith(values, 100, null);
        ScratchCardUtils.calculate(config, matrix);
        long expectedReward = 100 * 5;// E appears 3 times(1x) and on diagonal => 5x
        assertEquals(expectedReward, matrix.getReward());
    }

    @Test
    void testWithBonusSymbols() {
        String[][] values = {{"F", "F", "F"}, {"F", "MISS", "10x"}, {"+1000", "5x", "F"}};
        GameState matrix = matrixWith(values, 100, Arrays.asList("10x", "+1000", "5x", "MISS"));
        ScratchCardUtils.calculate(config, matrix);

        long expectedReward = 100 * 2 * 2; // 2x (5F) * 2 (horizontal line)
        expectedReward = expectedReward * 10 * 5 + 1000; // 10x and 5x + 1000
        assertEquals(expectedReward, matrix.getReward());
    }
}
