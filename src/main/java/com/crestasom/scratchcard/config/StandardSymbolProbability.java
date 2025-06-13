
package com.crestasom.scratchcard.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class StandardSymbolProbability extends SymbolProbability {
    private int column;
    private int row;
}

