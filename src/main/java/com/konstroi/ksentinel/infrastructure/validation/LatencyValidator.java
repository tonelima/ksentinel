package com.konstroi.ksentinel.infrastructure.validation;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ValidationRule;
import org.springframework.stereotype.Component;

@Component
public class LatencyValidator implements ResponseValidator {

    @Override
    public ValidationResult validate(HttpClientPort.HttpResponse response, ValidationRule rule) {
        if (rule.getMaxLatencyMs() == null) {
            return ValidationResult.ok();
        }
        if (response.latencyMs() <= rule.getMaxLatencyMs()) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(String.format(
                "Latency %dms exceeds max allowed %dms", response.latencyMs(), rule.getMaxLatencyMs()
        ));
    }

    @Override
    public boolean supports(ValidationRule.RuleType ruleType) {
        return ruleType == ValidationRule.RuleType.LATENCY;
    }
}
