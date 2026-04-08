package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.exception.AuthStrategyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;

@Component
@RequiredArgsConstructor
public class BearerTokenStrategy implements AuthStrategy {

    private final CredentialEncryptPort encryptPort;

    @Override
    public void applyAuth(HttpRequest.Builder requestBuilder, ApiConfig config) {
        AuthCredential credential = config.getCredential();
        if (credential == null || credential.getToken() == null) {
            throw new AuthStrategyException("Bearer auth requires a token");
        }
        String token = encryptPort.decrypt(credential.getToken());
        requestBuilder.header("Authorization", "Bearer " + token);
    }
}
