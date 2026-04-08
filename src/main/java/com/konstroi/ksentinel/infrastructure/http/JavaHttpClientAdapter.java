package com.konstroi.ksentinel.infrastructure.http;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.infrastructure.auth.AuthStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaHttpClientAdapter implements HttpClientPort {

    private final java.net.http.HttpClient httpClient;
    private final AuthStrategyFactory authStrategyFactory;
    private final ObjectMapper objectMapper;

    @Override
    public com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse execute(ApiConfig config) {
        long start = System.currentTimeMillis();
        try {
            if (config.getHttpMethod() == com.konstroi.ksentinel.domain.model.HttpMethod.PING) {
                return executePing(config, start);
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl()))
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()));

            applyHeaders(builder, config);
            applyMethod(builder, config);
            authStrategyFactory.resolve(config.getAuthType()).applyAuth(builder, config);

            java.net.http.HttpResponse<String> response = httpClient.send(builder.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;

            log.debug("HTTP {} {} -> {} ({}ms)", config.getHttpMethod(), config.getUrl(), response.statusCode(), latency);
            return new com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse(
                    response.statusCode(), response.body(), latency, null
            );
        } catch (java.net.http.HttpTimeoutException e) {
            long latency = System.currentTimeMillis() - start;
            log.warn("Timeout for {} after {}ms", config.getUrl(), latency);
            return new com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse(0, null, latency, "timeout: " + e.getMessage());
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("Error executing request to {}: {}", config.getUrl(), e.getMessage());
            return new com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse(0, null, latency, e.getMessage());
        }
    }

    private com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse executePing(ApiConfig config, long start) {
        long timeoutMs = Duration.ofSeconds(config.getTimeoutSeconds()).toMillis();
        try {
            String target = resolvePingTarget(config.getUrl());
            InetAddress address = InetAddress.getByName(target);
            boolean reachable = address.isReachable((int) timeoutMs);
            long latency = System.currentTimeMillis() - start;

            if (!reachable) {
                log.warn("Ping timeout for {} after {}ms", target, latency);
                return new com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse(
                        0, null, latency, "timeout: host did not respond to ping"
                );
            }

            log.debug("PING {} -> reachable ({}ms)", target, latency);
            return new com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse(
                    200, "PING OK", latency, null
            );
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("Error executing ping to {}: {}", config.getUrl(), e.getMessage());
            return new com.konstroi.ksentinel.application.port.out.HttpClientPort.HttpResponse(
                    0, null, latency, e.getMessage()
            );
        }
    }

    private String resolvePingTarget(String configuredUrl) {
        if (configuredUrl == null || configuredUrl.isBlank()) {
            throw new IllegalArgumentException("Ping target is required");
        }

        if (configuredUrl.contains("://")) {
            try {
                URI uri = new URI(configuredUrl);
                if (uri.getHost() == null || uri.getHost().isBlank()) {
                    throw new IllegalArgumentException("Invalid ping target: " + configuredUrl);
                }
                return uri.getHost();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid ping target: " + configuredUrl, e);
            }
        }

        return configuredUrl.trim();
    }

    @SuppressWarnings("unchecked")
    private void applyHeaders(HttpRequest.Builder builder, ApiConfig config) {
        builder.header("Content-Type", "application/json");
        builder.header("Accept", "application/json");
        if (config.getRequestHeaders() != null && !config.getRequestHeaders().isBlank()) {
            try {
                Map<String, String> headers = objectMapper.readValue(config.getRequestHeaders(), Map.class);
                headers.forEach(builder::header);
            } catch (Exception e) {
                log.warn("Could not parse requestHeaders for config id={}", config.getId());
            }
        }
    }

    private void applyMethod(HttpRequest.Builder builder, ApiConfig config) {
        String body = config.getRequestBody() != null ? config.getRequestBody() : "";
        switch (config.getHttpMethod()) {
            case GET    -> builder.GET();
            case DELETE -> builder.DELETE();
            case POST   -> builder.POST(HttpRequest.BodyPublishers.ofString(body));
            case PUT    -> builder.PUT(HttpRequest.BodyPublishers.ofString(body));
            case PATCH  -> builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body));
            case HEAD   -> builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
            default     -> builder.GET();
        }
    }
}
