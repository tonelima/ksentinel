package com.konstroi.ksentinel.infrastructure.notification;

import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.host")
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendAlert(ApiConfig config, MonitoringResult result) {
        if (config.getAlertEmail() == null || config.getAlertEmail().isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(config.getAlertEmail());
            message.setSubject(buildSubject(config, result));
            message.setText(buildBody(config, result));
            mailSender.send(message);
            log.info("Alert email sent to {} for API '{}'", config.getAlertEmail(), config.getName());
        } catch (Exception e) {
            log.error("Failed to send email alert for API '{}': {}", config.getName(), e.getMessage());
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.EMAIL;
    }

    private String buildSubject(ApiConfig config, MonitoringResult result) {
        String icon = result.getStatus() == MonitoringStatus.UP ? "✅ RECOVERED" : "🚨 DOWN";
        return String.format("[API Monitor] %s - %s", icon, config.getName());
    }

    private String buildBody(ApiConfig config, MonitoringResult result) {
        return String.format("""
                API Monitor Alert
                -----------------
                API Name : %s
                URL      : %s
                Status   : %s
                HTTP     : %s
                Latency  : %sms
                Time     : %s
                Error    : %s
                """,
                config.getName(),
                config.getUrl(),
                result.getStatus(),
                result.getHttpStatus() != null ? result.getHttpStatus() : "N/A",
                result.getLatencyMs() != null ? result.getLatencyMs() : "N/A",
                result.getCheckedAt(),
                result.getErrorMessage() != null ? result.getErrorMessage() : "none"
        );
    }
}