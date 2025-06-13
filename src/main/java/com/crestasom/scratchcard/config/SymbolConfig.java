
package com.crestasom.scratchcard.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SymbolConfig {
    @JsonProperty("reward_multiplier")
    private Double rewardMultiplier;

    private Integer extra;

    private String type;
    private String impact;
}
