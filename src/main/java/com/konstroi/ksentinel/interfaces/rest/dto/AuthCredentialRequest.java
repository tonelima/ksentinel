package com.konstroi.ksentinel.interfaces.rest.dto;

import com.konstroi.ksentinel.domain.model.AuthType;
import lombok.Data;

@Data
public class AuthCredentialRequest {
    private AuthType authType;
    private String username;
    private String password;
    private String token;
    private String apiKey;
    private String apiKeyHeader;
    private Boolean apiKeyAsQueryParam;
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String scope;
}
