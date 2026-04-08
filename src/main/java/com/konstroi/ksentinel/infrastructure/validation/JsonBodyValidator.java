package com.konstroi.ksentinel.infrastructure.validation;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ValidationRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonBodyValidator implements ResponseValidator {

    @Override
    public ValidationResult validate(HttpClientPort.HttpResponse response, ValidationRule rule) {
        if (response.body() == null || response.body().isBlank()) {
            return ValidationResult.fail("Response body is empty");
        }
        try {
            Object value = JsonPath.read(response.body(), rule.getJsonPath());
            String actual = value != null ? value.toString() : "null";

            if (rule.getExpectedValue() == null) {
                return ValidationResult.ok();
            }

            boolean matches = evaluate(actual, rule.getOperator(), rule.getExpectedValue());
            if (matches) {
                return ValidationResult.ok();
            }
            return ValidationResult.fail(String.format(
                    "JSONPath '%s': expected '%s %s' but got '%s'",
                    rule.getJsonPath(), rule.getOperator(), rule.getExpectedValue(), actual
            ));
        } catch (PathNotFoundException e) {
            return ValidationResult.fail("JSONPath not found: " + rule.getJsonPath());
        } catch (Exception e) {
            log.warn("JSONPath validation error: {}", e.getMessage());
            return ValidationResult.fail("JSONPath error: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(ValidationRule.RuleType ruleType) {
        return ruleType == ValidationRule.RuleType.JSON_BODY;
    }

    private boolean evaluate(String actual, String operator, String expected) {
        return switch (operator != null ? operator.toUpperCase() : "EQUALS") {
            case "EQUALS"      -> actual.equals(expected);
            case "NOT_EQUALS"  -> !actual.equals(expected);
            case "CONTAINS"    -> actual.contains(expected);
            case "NOT_CONTAINS"-> !actual.contains(expected);
            case "GT"          -> toDouble(actual) > toDouble(expected);
            case "LT"          -> toDouble(actual) < toDouble(expected);
            case "GTE"         -> toDouble(actual) >= toDouble(expected);
            case "LTE"         -> toDouble(actual) <= toDouble(expected);
            default            -> actual.equals(expected);
        };
    }

    private double toDouble(String val) {
        try { return Double.parseDouble(val); }
        catch (NumberFormatException e) { return 0; }
    }
}
