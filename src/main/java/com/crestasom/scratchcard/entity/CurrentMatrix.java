
package com.crestasom.scratchcard.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// @
// @Data
public class CurrentMatrix {

    private String[][] matrixValues;
    @JsonIgnore
    private Map<String, Integer> symbolCounts;
    // @JsonIgnore
    private List<String> bonusSymbols;
    private double reward;
    @JsonIgnore
    private long betAmount;
    private Map<String, List<String>> appliedWinningCombinations;

    public CurrentMatrix(int row, int col) {
        matrixValues = new String[row][col];
        symbolCounts = new HashMap<>();
        bonusSymbols = new ArrayList<>();
        appliedWinningCombinations = new HashMap<>();
    }

    public void setValue(int x, int y, String value) {
        matrixValues[x][y] = value;
    }

    public String getValue(int x, int y) {
        return matrixValues[x][y];
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


    public String[][] getMatrixValues() {
        return matrixValues;
    }

    @JsonIgnore
    public Set<String> getSymbols() {
        return symbolCounts.keySet();
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            // ObjectWriter writer = mapper.writer(prettyPrinter);
            // return writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
        // StringBuffer val = new StringBuffer("{\n'matrix':[\n");
        // for (int i = 0; i < matrixValues.length; i++) {
        // val.append("[");
        // for (int j = 0; j < matrixValues[i].length; j++) {
        // val.append("'").append(matrixValues[i][j]).append("'");
        // val.append(j != matrixValues[i].length - 1 ? "," : "");
        // }
        // val.append(i != matrixValues.length - 1 ? "]\n" : "]");
        // }
        // val.append("\n],");
        // val.append("'reward':").append((long) reward).append(",");
        // val.append("'applied_winning_combinations':{");
        // for (Map.Entry<String, List<String>> entry : appliedWinningCombinations.entrySet()) {
        //
        // }
        // val.append("\n}");
        // return val.toString().replace("'", "\"");
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

    public void setAppliedWinningCombinations(Map<String, List<String>> appliedWinningCombinations) {
        this.appliedWinningCombinations = appliedWinningCombinations;
    }

    public void setMatrixValues(String[][] matrixValues) {
        this.matrixValues = matrixValues;
    }

    public void setBonusSymbols(List<String> bonusSymbols) {
        this.bonusSymbols = bonusSymbols;
    }

}

