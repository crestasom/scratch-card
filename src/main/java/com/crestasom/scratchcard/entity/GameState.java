
package com.crestasom.scratchcard.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class GameState {

    private String[][] matrix;
    @JsonIgnore
    private Map<String, Integer> symbolCounts;
    // @JsonIgnore
    @JsonProperty(value = "applied_bonus_symbol")
    private List<String> bonusSymbols;
    private double reward;
    @JsonIgnore
    private long betAmount;
    @JsonProperty(value = "applied_winning_combinations")
    private Map<String, List<String>> appliedWinningCombinations;

    public GameState(int row, int col) {
        matrix = new String[row][col];
        symbolCounts = new HashMap<>();
        bonusSymbols = new ArrayList<>();
        appliedWinningCombinations = new HashMap<>();
    }

    public void setValue(int x, int y, String value) {
        matrix[x][y] = value;
    }

    public String getValue(int x, int y) {
        return matrix[x][y];
    }

    public void incrementSymbolCount(String symbol) {
        symbolCounts.merge(symbol, 1, Integer::sum);
    }

    public void decrementSymbolCount(String symbol) {
        symbolCounts.merge(symbol, -1, Integer::sum);
        if (symbolCounts.get(symbol) <= 0) {
            symbolCounts.remove(symbol);
        }
    }

    public int getSymbolCount(String symbol) {
        return symbolCounts.getOrDefault(symbol, 0);
    }


    @JsonIgnore
    public Set<String> getSymbols() {
        return symbolCounts.keySet();
    }

    public void addBonusSymbol(String symbol) {
        bonusSymbols.add(symbol);
    }

    public List<String> getBonusSymbols() {
        return bonusSymbols;
    }

    public boolean hasBonusSymbol() {
        return !bonusSymbols.isEmpty();
    }

    public long getReward() {
        return (long) reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    public Map<String, List<String>> getAppliedWinningCombinations() {
        return appliedWinningCombinations;
    }

    public String[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(String[][] matrixValues) {
        this.matrix = matrixValues;
    }

    public void setBonusSymbols(List<String> bonusSymbols) {
        this.bonusSymbols = bonusSymbols;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("cannot create to string for game state class");
        }
    }
}

