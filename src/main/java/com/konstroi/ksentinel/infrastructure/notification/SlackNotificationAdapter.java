package com.konstroi.ksentinel.infrastructure.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackNotificationAdapter implements NotificationPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${monitoring.slack.webhook-url:}")
    private String slackWebhookUrl;

    @Override
    public void sendAlert(ApiConfig config, MonitoringResult result) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) {
            return;
        }
        try {
            boolean isDown = result.getStatus() != MonitoringStatus.UP;
            String color  = isDown ? "#FF0000" : "#36a64f";
            String title  = isDown
                    ? ":red_circle: API DOWN: " + config.getName()
                    : ":white_check_mark: API RECOVERED: " + config.getName();

            Map<String, Object> attachment = Map.of(
                    "color",  color,
                    "title",  title,
                    "fields", List.of(
                            field("URL",     config.getUrl(),       true),
                            field("Status",  result.getStatus().name(), true),
                            field("HTTP",    String.valueOf(result.getHttpStatus() != null ? result.getHttpStatus() : "N/A"), true),
                            field("Latency", (result.getLatencyMs() != null ? result.getLatencyMs() : 0) + "ms", true),
                            field("Error",   result.getErrorMessage() != null ? result.getErrorMessage() : "none", false)
                    ),
                    "footer", "API Monitor",
                    "ts",     System.currentTimeMillis() / 1000
            );

            String body = objectMapper.writeValueAsString(Map.of("attachments", List.of(attachment)));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(slackWebhookUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Slack alert sent for API '{}', response: {}", config.getName(), response.statusCode());
        } catch (Exception e) {
            log.error("Failed to send Slack alert for API '{}': {}", config.getName(), e.getMessage());
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.SLACK;
    }

    private Map<String, Object> field(String title, String value, boolean shortField) {
        return Map.of("title", title, "value", value, "short", shortField);
    }
}
