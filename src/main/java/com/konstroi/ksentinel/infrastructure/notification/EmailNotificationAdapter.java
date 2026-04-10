package com.konstroi.ksentinel.infrastructure.notification;

import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AppUser;
import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final CredentialEncryptPort encryptPort;

    @Override
    public void sendAlert(ApiConfig config, MonitoringResult result) {
        Set<String> recipients = recipients(config);
        if (recipients.isEmpty()) {
            return;
        }

        AppUser user = smtpUser(config);
        if (!isSmtpConfigured(user)) {
            log.warn("Skipping email alert for API '{}': SMTP is not configured for the owner user", config.getName());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipients.toArray(String[]::new));
            message.setFrom(fromEmail(user));
            message.setSubject(buildSubject(config, result));
            message.setText(buildBody(config, result));
            mailSender(user).send(message);
            log.info("Alert email sent to {} for API '{}'", recipients, config.getName());
        } catch (Exception e) {
            log.error("Failed to send email alert for API '{}': {}", config.getName(), e.getMessage());
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.EMAIL;
    }

    private String buildSubject(ApiConfig config, MonitoringResult result) {
        String status = result.getStatus() == MonitoringStatus.UP ? "RECOVERED" : "DOWN";
        return String.format("[API Monitor] %s - %s", status, config.getName());
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

    private JavaMailSender mailSender(AppUser user) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(user.getSmtpHost());
        sender.setPort(user.getSmtpPort());
        sender.setUsername(user.getSmtpUsername());
        sender.setPassword(encryptPort.decrypt(user.getSmtpPassword()));

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(Boolean.TRUE.equals(user.getSmtpAuth())));
        properties.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(user.getSmtpStarttls())));
        return sender;
    }

    private Set<String> recipients(ApiConfig config) {
        Set<String> recipients = new LinkedHashSet<>();
        Company company = config.getCompany();
        if (company != null && company.getNotificationEmails() != null) {
            recipients.addAll(company.getNotificationEmails());
        }
        if (config.getAlertEmail() != null && !config.getAlertEmail().isBlank()) {
            recipients.add(config.getAlertEmail().trim());
        }
        return recipients;
    }

    private AppUser smtpUser(ApiConfig config) {
        return config.getCompany() == null ? null : config.getCompany().getUser();
    }

    private boolean isSmtpConfigured(AppUser user) {
        return user != null
                && user.getSmtpHost() != null
                && !user.getSmtpHost().isBlank()
                && user.getSmtpPort() != null;
    }

    private String fromEmail(AppUser user) {
        if (user.getSmtpFromEmail() != null && !user.getSmtpFromEmail().isBlank()) {
            return user.getSmtpFromEmail();
        }
        return user.getEmail();
    }
}
