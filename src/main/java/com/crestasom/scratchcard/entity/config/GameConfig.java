
package com.crestasom.scratchcard.entity.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GameConfig {
    private int columns;
    private int rows;
    private Map<String, SymbolConfig> symbols;
    private ProbabilitiesConfig probabilities;

    @JsonProperty("win_combinations")
    private Map<String, WinCombination> winCombinations;
}
