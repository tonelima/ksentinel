package com.konstroi.ksentinel.infrastructure.validation;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidatorChain {

    private final List<ResponseValidator> validators;

    public ValidationResult validate(HttpClientPort.HttpResponse response, List<ValidationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            // default: any 2xx is success
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return ValidationResult.ok();
            }
            return ValidationResult.fail("HTTP " + response.statusCode());
        }

        List<ValidationResult> results = rules.stream()
                .map(rule -> applyRule(response, rule))
                .toList();

        return ValidationResult.aggregate(results);
    }

    private ValidationResult applyRule(HttpClientPort.HttpResponse response, ValidationRule rule) {
        return validators.stream()
                .filter(v -> v.supports(rule.getRuleType()))
                .findFirst()
                .map(v -> v.validate(response, rule))
                .orElseGet(() -> {
                    log.warn("No validator found for rule type: {}", rule.getRuleType());
                    return ValidationResult.ok();
                });
    }
}
