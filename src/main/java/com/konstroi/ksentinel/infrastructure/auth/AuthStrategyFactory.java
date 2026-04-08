package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.domain.model.AuthType;
import com.konstroi.ksentinel.exception.AuthStrategyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthStrategyFactory {

    private final NoAuthStrategy noAuthStrategy;
    private final BasicAuthStrategy basicAuthStrategy;
    private final BearerTokenStrategy bearerTokenStrategy;
    private final ApiKeyStrategy apiKeyStrategy;
    private final OAuth2Strategy oAuth2Strategy;

    public AuthStrategy resolve(AuthType authType) {
        return switch (authType) {
            case NONE   -> noAuthStrategy;
            case BASIC  -> basicAuthStrategy;
            case BEARER -> bearerTokenStrategy;
            case API_KEY -> apiKeyStrategy;
            case OAUTH2 -> oAuth2Strategy;
            default -> throw new AuthStrategyException("Unsupported auth type: " + authType);
        };
    }
}
