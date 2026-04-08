package com.konstroi.ksentinel.infrastructure.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.exception.AuthStrategyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2Strategy implements AuthStrategy {

    private final CredentialEncryptPort encryptPort;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public void applyAuth(HttpRequest.Builder requestBuilder, ApiConfig config) {
        AuthCredential credential = config.getCredential();
        if (credential == null || credential.getClientId() == null) {
            throw new AuthStrategyException("OAuth2 requires clientId, clientSecret and tokenUrl");
        }

        String token = resolveToken(credential);
        requestBuilder.header("Authorization", "Bearer " + token);
    }

    private String resolveToken(AuthCredential credential) {
        if (isTokenValid(credential)) {
            return credential.getCachedToken();
        }
        return fetchAndCacheToken(credential);
    }

    private boolean isTokenValid(AuthCredential credential) {
        return credential.getCachedToken() != null
                && credential.getTokenExpiresAt() != null
                && credential.getTokenExpiresAt().isAfter(LocalDateTime.now().plusSeconds(30));
    }

    private String fetchAndCacheToken(AuthCredential credential) {
        try {
            String clientSecret = encryptPort.decrypt(credential.getClientSecret());
            String body = "grant_type=client_credentials"
                    + "&client_id=" + credential.getClientId()
                    + "&client_secret=" + clientSecret
                    + (credential.getScope() != null ? "&scope=" + credential.getScope() : "");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(credential.getTokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

            String accessToken = json.get("access_token").asText();
            int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt(3600) : 3600;

            credential.setCachedToken(accessToken);
            credential.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));

            log.debug("Fetched new OAuth2 token, expires in {}s", expiresIn);
            return accessToken;
        } catch (Exception e) {
            throw new AuthStrategyException("Failed to fetch OAuth2 token: " + e.getMessage());
        }
    }
}
