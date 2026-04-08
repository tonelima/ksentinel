package com.konstroi.ksentinel.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.konstroi.ksentinel.domain.model.AuthType;
import com.konstroi.ksentinel.domain.model.HttpMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApiConfigResponse {
    private Long id;
    private String name;
    private CompanySummaryResponse company;
    private String url;
    private HttpMethod httpMethod;
    private Integer intervalSeconds;
    private Integer timeoutSeconds;
    private Boolean enabled;
    private AuthType authType;
    private String alertEmail;
    private String alertWebhookUrl;
    private String description;
    private Integer consecutiveFailures;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastCheckedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedAt;
    private AuthCredentialResponse credential;
    private List<ValidationRuleResponse> validationRules;

    @Data
    public static class CompanySummaryResponse {
        private Long id;
        private String name;
    }

    @Data
    public static class ValidationRuleResponse {
        private Long id;
        private String ruleType;
        private String jsonPath;
        private String operator;
        private String expectedValue;
        private Integer maxLatencyMs;
        private Integer expectedStatus;
        private String description;
    }
}
