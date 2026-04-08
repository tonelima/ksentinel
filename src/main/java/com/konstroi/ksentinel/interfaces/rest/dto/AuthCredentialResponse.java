package com.konstroi.ksentinel.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.konstroi.ksentinel.domain.model.AuthType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthCredentialResponse {
    private Long id;
    private AuthType authType;
    private String username;
    private String apiKeyHeader;
    private Boolean apiKeyAsQueryParam;
    private String clientId;
    private String tokenUrl;
    private String scope;
    private boolean hasPassword;
    private boolean hasToken;
    private boolean hasApiKey;
    private boolean hasClientSecret;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime tokenExpiresAt;
}
