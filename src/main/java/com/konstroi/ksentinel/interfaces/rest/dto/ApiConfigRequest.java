package com.konstroi.ksentinel.interfaces.rest.dto;

import com.konstroi.ksentinel.domain.model.AuthType;
import com.konstroi.ksentinel.domain.model.HttpMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ApiConfigRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotBlank(message = "URL is required")
    @Size(max = 2048)
    private String url;

    private HttpMethod httpMethod = HttpMethod.GET;

    @Min(value = 5, message = "Interval must be at least 5 seconds")
    @Max(value = 86400, message = "Interval must be at most 86400 seconds (1 day)")
    private Integer intervalSeconds = 60;

    @Min(5) @Max(120)
    private Integer timeoutSeconds = 10;

    private Boolean enabled = true;

    private AuthType authType = AuthType.NONE;

    private String requestHeaders;
    private String requestBody;

    @Email(message = "Invalid alert email")
    private String alertEmail;

    private String alertWebhookUrl;

    @Size(max = 1024)
    private String description;

    private AuthCredentialRequest credential;
    private List<ValidationRuleRequest> validationRules;
}
