package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.exception.AuthStrategyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;

@Component
@RequiredArgsConstructor
public class ApiKeyStrategy implements AuthStrategy {

    private final CredentialEncryptPort encryptPort;

    @Override
    public void applyAuth(HttpRequest.Builder requestBuilder, ApiConfig config) {
        AuthCredential credential = config.getCredential();
        if (credential == null || credential.getApiKey() == null) {
            throw new AuthStrategyException("API Key auth requires an apiKey");
        }

        String key = encryptPort.decrypt(credential.getApiKey());
        String headerName = credential.getApiKeyHeader() != null ? credential.getApiKeyHeader() : "X-Api-Key";

        if (Boolean.TRUE.equals(credential.getApiKeyAsQueryParam())) {
            URI original = requestBuilder.build().uri();
            String separator = original.getQuery() == null ? "?" : "&";
            URI newUri = URI.create(original + separator + headerName + "=" + key);
            requestBuilder.uri(newUri);
        } else {
            requestBuilder.header(headerName, key);
        }
    }
}
