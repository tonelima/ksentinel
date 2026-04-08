package com.konstroi.ksentinel.infrastructure.validation;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ValidationRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorChainTest {

    private ValidatorChain chain;

    @BeforeEach
    void setUp() {
        chain = new ValidatorChain(List.of(
                new StatusCodeValidator(),
                new JsonBodyValidator(),
                new LatencyValidator()
        ));
    }

    @Test
    void validate_noRules_2xx_passes() {
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{}", 100L, null);
        ValidationResult result = chain.validate(resp, List.of());
        assertThat(result.passed()).isTrue();
    }

    @Test
    void validate_noRules_4xx_fails() {
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(404, "Not Found", 80L, null);
        ValidationResult result = chain.validate(resp, List.of());
        assertThat(result.passed()).isFalse();
    }

    @Test
    void validate_statusCodeRule_matchingStatus_passes() {
        ValidationRule rule = ValidationRule.builder()
                .ruleType(ValidationRule.RuleType.STATUS_CODE)
                .expectedStatus(201)
                .build();
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(201, "{}", 50L, null);

        ValidationResult result = chain.validate(resp, List.of(rule));

        assertThat(result.passed()).isTrue();
    }

    @Test
    void validate_statusCodeRule_wrongStatus_fails() {
        ValidationRule rule = ValidationRule.builder()
                .ruleType(ValidationRule.RuleType.STATUS_CODE)
                .expectedStatus(200)
                .build();
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(500, "error", 200L, null);

        ValidationResult result = chain.validate(resp, List.of(rule));

        assertThat(result.passed()).isFalse();
        assertThat(result.details()).contains("500");
    }

    @Test
    void validate_jsonBodyRule_matchingValue_passes() {
        ValidationRule rule = ValidationRule.builder()
                .ruleType(ValidationRule.RuleType.JSON_BODY)
                .jsonPath("$.status")
                .operator("EQUALS")
                .expectedValue("ok")
                .build();
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{\"status\":\"ok\"}", 90L, null);

        ValidationResult result = chain.validate(resp, List.of(rule));

        assertThat(result.passed()).isTrue();
    }

    @Test
    void validate_jsonBodyRule_missingPath_fails() {
        ValidationRule rule = ValidationRule.builder()
                .ruleType(ValidationRule.RuleType.JSON_BODY)
                .jsonPath("$.nonexistent")
                .operator("EQUALS")
                .expectedValue("value")
                .build();
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{\"status\":\"ok\"}", 90L, null);

        ValidationResult result = chain.validate(resp, List.of(rule));

        assertThat(result.passed()).isFalse();
    }

    @Test
    void validate_latencyRule_withinLimit_passes() {
        ValidationRule rule = ValidationRule.builder()
                .ruleType(ValidationRule.RuleType.LATENCY)
                .maxLatencyMs(500)
                .build();
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{}", 300L, null);

        ValidationResult result = chain.validate(resp, List.of(rule));

        assertThat(result.passed()).isTrue();
    }

    @Test
    void validate_latencyRule_exceeded_fails() {
        ValidationRule rule = ValidationRule.builder()
                .ruleType(ValidationRule.RuleType.LATENCY)
                .maxLatencyMs(200)
                .build();
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{}", 800L, null);

        ValidationResult result = chain.validate(resp, List.of(rule));

        assertThat(result.passed()).isFalse();
        assertThat(result.details()).contains("800ms");
    }

    @Test
    void validate_multipleRules_allPass_returnsOk() {
        List<ValidationRule> rules = List.of(
                ValidationRule.builder().ruleType(ValidationRule.RuleType.STATUS_CODE).expectedStatus(200).build(),
                ValidationRule.builder().ruleType(ValidationRule.RuleType.LATENCY).maxLatencyMs(1000).build(),
                ValidationRule.builder().ruleType(ValidationRule.RuleType.JSON_BODY)
                        .jsonPath("$.ok").operator("EQUALS").expectedValue("true").build()
        );
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{\"ok\":\"true\"}", 100L, null);

        ValidationResult result = chain.validate(resp, rules);

        assertThat(result.passed()).isTrue();
    }

    @Test
    void validate_multipleRules_oneFails_aggregatesFailures() {
        List<ValidationRule> rules = List.of(
                ValidationRule.builder().ruleType(ValidationRule.RuleType.STATUS_CODE).expectedStatus(200).build(),
                ValidationRule.builder().ruleType(ValidationRule.RuleType.LATENCY).maxLatencyMs(100).build()
        );
        HttpClientPort.HttpResponse resp = new HttpClientPort.HttpResponse(200, "{}", 500L, null);

        ValidationResult result = chain.validate(resp, rules);

        assertThat(result.passed()).isFalse();
        assertThat(result.details()).contains("500ms");
    }
}
