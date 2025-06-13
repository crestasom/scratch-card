
package com.crestasom.scratchcard.config;

import java.util.Map;

import lombok.Data;

@Data
public abstract class SymbolProbability {
    protected Map<String, Integer> symbols;
}
