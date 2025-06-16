
package com.crestasom.scratchcard.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.crestasom.scratchcard.entity.GameState;
import com.crestasom.scratchcard.entity.config.GameConfig;
import com.crestasom.scratchcard.entity.config.ScratchCardConstants;
import com.crestasom.scratchcard.entity.config.SymbolProbability;
import com.crestasom.scratchcard.entity.config.WinCombination;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScratchCardUtils {
    private static int rowNum = 3;
    private static int colNum = 3;
    private static int maxBonusSymbol = 3;

    public static GameConfig loadConfig(String filePath) throws IOException {
        rowNum = ConfigReader.getInt("row.num");
        colNum = ConfigReader.getInt("col.num");
        maxBonusSymbol = ConfigReader.getInt("max.bonus.symbol");
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Config file not found at path: " + file.getAbsolutePath());
        }

        return mapper.readValue(file, GameConfig.class);
    }

    public static GameState initMatrix(GameConfig gameConfig) {
        GameState matrix = new GameState(rowNum, colNum);
        gameConfig.getProbabilities().getStandardSymbols().stream()
                .forEach(g -> initCurrentSymbol(g, matrix, gameConfig, g.getRow(), g.getColumn(), false));
        return matrix;
    }

    private static String selectSymbolWeighted(SymbolProbability probability) {
        int totalWeight = probability.getSymbols().values().stream().mapToInt(Integer::intValue).sum();
        int rand = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulativeWt = 0;
        for (Map.Entry<String, Integer> entry : probability.getSymbols().entrySet()) {
            cumulativeWt += entry.getValue();
            if (rand < cumulativeWt)
                return entry.getKey();
        }
        throw new IllegalStateException("Unable to select symbol based on weight");
    }


    public static void initCurrentSymbol(SymbolProbability symbolProbability, GameState matrix,
            GameConfig gameConfig, int row, int column, Boolean isBonus) {

        log.debug("init current symbol for [{}][{}]", row + 1, column + 1);

        String symbol = selectSymbolWeighted(symbolProbability);

        log.debug("setting [{}] in [{}][{}]", symbol, row + 1, column + 1);
        if (isBonus) {
            String currentSymbol = matrix.getValue(row, column);
            matrix.decrementSymbolCount(currentSymbol);
            matrix.addBonusSymbol(symbol);
        } else {
            matrix.incrementSymbolCount(symbol);
        }
        matrix.setValue(row, column, symbol);

    }

    public static void addBonusSymbol(GameConfig gameConfig, GameState gameState) {
        List<int[]> bonusPositions = pickBonusPositions(rowNum, colNum);
        log.debug("bonusPositions [{}]", bonusPositions);
        bonusPositions.forEach(b -> {
            int r = b[0];
            int c = b[1];
            initCurrentSymbol(gameConfig.getProbabilities().getBonusSymbols(), gameState, gameConfig, r, c, true);
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
        int numberOfBonusSymbol = ThreadLocalRandom.current().nextInt(maxBonusSymbol + 1);
        Collections.shuffle(all, ThreadLocalRandom.current());
        return all.subList(0, numberOfBonusSymbol);
    }

    public static void calculate(GameConfig gameConfig, GameState gameState) {
        Map<String, List<String>> matchedCombo = gameState.getAppliedWinningCombinations();
        Map<String, WinCombination> sameSymbolCombo = gameConfig.getWinCombinations().entrySet().stream()
                .filter(f -> ScratchCardConstants.SAME_SYMBOLS.equals(f.getValue().getWhen()))
                .sorted(Comparator.comparing((Map.Entry<String, WinCombination> entry) -> entry.getValue().getCount())
                        .reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        log.debug("sameSymbolCombo [{}]", sameSymbolCombo);
        Map<String, WinCombination> linearSymbolCombo = gameConfig.getWinCombinations().entrySet().stream()
                .filter(f -> ScratchCardConstants.LINEAR_SYMBOLS.equals(f.getValue().getWhen()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        log.debug("linearSymbolCombo [{}]", linearSymbolCombo);
        
        for (String symbol : gameState.getSymbols()) {
            sameSymbolCombo.entrySet().stream()
            .filter(e -> gameState.getSymbolCount(symbol) >= e.getValue().getCount())
            .findFirst()
                    .ifPresent(e -> matchedCombo.put(symbol, new ArrayList<>(Arrays.asList(e.getKey()))));
        }
        log.debug("matchedCombo after same symbol check [{}]", matchedCombo);
        for (String symbol : gameState.getSymbols()) {
            List<String> matchedLinearCombo = matchLinearCombo(linearSymbolCombo, symbol, gameState);
            if (matchedLinearCombo != null && matchedLinearCombo.size() > 0) {
                List<String> currentVal = matchedCombo.get(symbol);
                if (currentVal == null) {
                    currentVal = new ArrayList<>();
                    matchedCombo.put(symbol, currentVal);
                }
                currentVal.addAll(matchedLinearCombo);
            }
        }
        log.debug("matchedCombo after linear check [{}]", matchedCombo);
        if (!matchedCombo.isEmpty()) {
            calculateReward(gameState, gameConfig);
        }
    }

   
    private static List<String> matchLinearCombo(Map<String, WinCombination> linearSymbolCombo, String symbol,
            GameState gameState) {
        List<String> matchedLinearCombo = new ArrayList<>();
        for (Map.Entry<String, WinCombination> entrySet : linearSymbolCombo.entrySet()) {
            for (List<String> position : entrySet.getValue().getCoveredAreas()) {

                boolean allMatch = position.stream().allMatch(p -> {
                    String[] pos = p.split(":");
                    return symbol
                            .equalsIgnoreCase(gameState.getValue(Integer.parseInt(pos[0]), Integer.parseInt(pos[1])));
                });
                if (allMatch) {
                    matchedLinearCombo.add(entrySet.getKey());
                    break;
                }
            }
        }
        return matchedLinearCombo;
        
    }

    public static void calculateReward(GameState gameState, GameConfig gameConfig) {
        Map<String, List<String>> matchedCombo = gameState.getAppliedWinningCombinations();
        long betAmt = gameState.getBetAmount();
        double reward = 0;
        for (Map.Entry<String, List<String>> entry : matchedCombo.entrySet()) {
            double rewardMultiplier = calculateRewardMultiplier(entry.getValue(), gameConfig);
            reward += betAmt * rewardMultiplier;
        }
        if (gameState.hasBonusSymbol()) {

            int multiplier = 1;
            int addition = 0;
            for (String b : gameState.getBonusSymbols()) {
                if (b.endsWith("x")) {
                    multiplier *= Integer.parseInt(b.replace("x", ""));
                } else if (b.startsWith("+")) {
                    addition += Integer.parseInt(b.substring(1));
                }
            }
            reward = reward * multiplier + addition;
        }

        gameState.setReward(reward);

    }

    public static double calculateRewardMultiplier(List<String> winCombo, GameConfig config) {
        return winCombo.stream().map(win -> config.getWinCombinations().get(win).getRewardMultiplier()).reduce(1.0,
                (a, b) -> a * b);
    }
}
