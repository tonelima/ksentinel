package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final List<NotificationPort> notificationPorts;

    public void handleFailure(ApiConfig config, MonitoringResult result, int previousConsecutiveFailures) {
        if (shouldSendFailureAlert(config, previousConsecutiveFailures)) {
            log.warn("API id={} name={} reached notification delay ({} minutes), sending alert",
                    config.getId(), config.getName(), notificationDelayMinutes(config));
            sendAlert(config, result);
        }
    }

    public void handleRecovery(ApiConfig config, MonitoringResult result, int previousConsecutiveFailures) {
        if (previousConsecutiveFailures > 0) {
            log.info("API id={} name={} recovered, sending recovery notification", config.getId(), config.getName());
            sendAlert(config, result);
        }
    }

    private void sendAlert(ApiConfig config, MonitoringResult result) {
        notificationPorts.forEach(port -> {
            try {
                port.sendAlert(config, result);
            } catch (Exception e) {
                log.error("Failed to send notification via {}", port.getClass().getSimpleName(), e);
            }
        });
    }

    private boolean shouldSendFailureAlert(ApiConfig config, int previousConsecutiveFailures) {
        int currentFailures = config.getConsecutiveFailures() == null ? 0 : config.getConsecutiveFailures();
        if (currentFailures <= 0) {
            return false;
        }

        int delayMinutes = notificationDelayMinutes(config);
        if (delayMinutes <= 0) {
            return currentFailures == 1;
        }

        long intervalSeconds = Math.max(1, config.getIntervalSeconds() == null ? 60 : config.getIntervalSeconds());
        long previousUnavailableSeconds = previousConsecutiveFailures * intervalSeconds;
        long currentUnavailableSeconds = currentFailures * intervalSeconds;
        long requiredUnavailableSeconds = delayMinutes * 60L;

        return previousUnavailableSeconds < requiredUnavailableSeconds
                && currentUnavailableSeconds >= requiredUnavailableSeconds;
    }

    private int notificationDelayMinutes(ApiConfig config) {
        return config.getNotificationDelayMinutes() == null ? 0 : config.getNotificationDelayMinutes();
    }
}
