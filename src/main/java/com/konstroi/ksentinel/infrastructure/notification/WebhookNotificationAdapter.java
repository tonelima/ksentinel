package com.konstroi.ksentinel.infrastructure.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookNotificationAdapter implements NotificationPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public void sendAlert(ApiConfig config, MonitoringResult result) {
        if (config.getAlertWebhookUrl() == null || config.getAlertWebhookUrl().isBlank()) {
            return;
        }
        try {
            Map<String, Object> payload = Map.of(
                    "apiId",      config.getId(),
                    "apiName",    config.getName(),
                    "url",        config.getUrl(),
                    "status",     result.getStatus().name(),
                    "httpStatus", result.getHttpStatus() != null ? result.getHttpStatus() : 0,
                    "latencyMs",  result.getLatencyMs() != null ? result.getLatencyMs() : 0,
                    "checkedAt",  result.getCheckedAt().toString(),
                    "error",      result.getErrorMessage() != null ? result.getErrorMessage() : ""
            );

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getAlertWebhookUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Webhook alert sent to {} for API '{}', response: {}",
                    config.getAlertWebhookUrl(), config.getName(), response.statusCode());
        } catch (Exception e) {
            log.error("Failed to send webhook alert for API '{}': {}", config.getName(), e.getMessage());
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.WEBHOOK;
    }
}
