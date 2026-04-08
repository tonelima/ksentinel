package com.konstroi.ksentinel.interfaces.rest.dto;

import com.konstroi.ksentinel.domain.model.ValidationRule;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidationRuleRequest {
    @NotNull
    private ValidationRule.RuleType ruleType;
    private String jsonPath;
    private String operator;
    private String expectedValue;
    private Integer maxLatencyMs;
    private Integer expectedStatus;
    private String description;
}
