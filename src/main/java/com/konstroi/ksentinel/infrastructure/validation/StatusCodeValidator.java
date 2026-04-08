package com.konstroi.ksentinel.infrastructure.validation;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ValidationRule;
import org.springframework.stereotype.Component;

@Component
public class StatusCodeValidator implements ResponseValidator {

    @Override
    public ValidationResult validate(HttpClientPort.HttpResponse response, ValidationRule rule) {
        int expected = rule.getExpectedStatus() != null ? rule.getExpectedStatus() : 200;
        if (response.statusCode() == expected) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(
                String.format("Expected HTTP %d but got %d", expected, response.statusCode())
        );
    }

    @Override
    public boolean supports(ValidationRule.RuleType ruleType) {
        return ruleType == ValidationRule.RuleType.STATUS_CODE;
    }
}
