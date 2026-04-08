package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.exception.AuthStrategyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class BasicAuthStrategy implements AuthStrategy {

    private final CredentialEncryptPort encryptPort;

    @Override
    public void applyAuth(HttpRequest.Builder requestBuilder, ApiConfig config) {
        AuthCredential credential = config.getCredential();
        if (credential == null || credential.getUsername() == null) {
            throw new AuthStrategyException("Basic auth requires username and password");
        }
        String password = encryptPort.decrypt(credential.getPassword());
        String encoded = Base64.getEncoder().encodeToString(
                (credential.getUsername() + ":" + password).getBytes()
        );
        requestBuilder.header("Authorization", "Basic " + encoded);
    }
}
