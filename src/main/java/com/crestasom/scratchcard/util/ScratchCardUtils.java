
package com.crestasom.scratchcard.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.crestasom.scratchcard.config.ConfigReader;
import com.crestasom.scratchcard.config.GameConfig;
import com.crestasom.scratchcard.config.SymbolProbability;
import com.crestasom.scratchcard.entity.CurrentMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScratchCardUtils {
    private static int rowNum = 3;
    private static int colNum = 3;
    private static Random random = new Random();
    private static int maxBonusSymbol = 3;

    public static GameConfig loadConfig() throws IOException {
        rowNum = ConfigReader.getInt("row.num");
        colNum = ConfigReader.getInt("col.num");
        maxBonusSymbol = ConfigReader.getInt("max.bonus.symbol");
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = ScratchCardUtils.class.getResourceAsStream("/config.json")) {
            if (is == null) {
                throw new IOException("Config file not found: config.json");
            }
            return mapper.readValue(is, GameConfig.class);
        }
    }

    public static CurrentMatrix initMatrix(GameConfig gameConfig) {
        CurrentMatrix matrix = new CurrentMatrix(rowNum, colNum);
        gameConfig.getProbabilities().getStandardSymbols().stream()
                .forEach(g -> initCurrentSymbol(g, matrix, gameConfig, g.getRow(), g.getColumn()));
        return matrix;
    }


    public static void initCurrentSymbol(SymbolProbability symbolProbability, CurrentMatrix matrix,
            GameConfig gameConfig, int row, int column) {


        log.debug("init current symbol for [{}][{}]", row + 1, column + 1);
        int totalWeight = symbolProbability.getSymbols().values().stream().mapToInt(Integer::intValue).sum();
        int rand = random.nextInt(totalWeight);
        log.debug("rand [{}]", rand);
        int cumulativeWt = 0;
        String symbol = null;
        for (Map.Entry<String, Integer> entry : symbolProbability.getSymbols().entrySet()) {
            cumulativeWt += entry.getValue();
            if (rand < cumulativeWt) {
                log.debug("symbol to be init [{}]", symbol);
                symbol = entry.getKey();
                break;
            }
        }

        if (symbol == null) {
            throw new RuntimeException(String.format("cannot init symbol for %d %d", row + 1, column + 1));
        }
        log.debug("setting [{}] in [{}][{}]", symbol, row + 1, column + 1);
        matrix.setValue(row, column, symbol);

    }



    public static void addBonusSymbol(GameConfig gameConfig, CurrentMatrix currentMatrix) {
        List<int[]> bonusPositions = pickBonusPositions(rowNum, colNum);
        gameConfig.getProbabilities().getBonusSymbols();
        bonusPositions.forEach(b -> {
            int r = b[0];
            int c = b[1];
            initCurrentSymbol(gameConfig.getProbabilities().getBonusSymbols(), currentMatrix, gameConfig, r, c);
        });
    }

    public static List<int[]> pickBonusPositions(int rows, int cols) {
        if (maxBonusSymbol > rows * cols) {
            throw new IllegalArgumentException("Cannot place more bonus symbols than cells");
        }
        List<int[]> all = new ArrayList<>(rows * cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                all.add(new int[] {r, c});
            }
        }
        int numberOfBonusSymbol = random.nextInt(maxBonusSymbol + 1);
        Collections.shuffle(all, random);
        return all.subList(0, numberOfBonusSymbol);
    }
}
