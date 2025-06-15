
package com.crestasom.scratchcard.entity.config;

import java.util.Map;

import lombok.Data;

@Data
public abstract class SymbolProbability {
    protected Map<String, Integer> symbols;
}
