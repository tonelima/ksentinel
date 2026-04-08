package com.konstroi.ksentinel.infrastructure.validation;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ValidationRule;

public interface ResponseValidator {

    ValidationResult validate(HttpClientPort.HttpResponse response, ValidationRule rule);

    boolean supports(ValidationRule.RuleType ruleType);
}
