package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final List<NotificationPort> notificationPorts;

    @Value("${monitoring.alert.consecutive-failures-threshold:3}")
    private int failuresThreshold;

    public void handleFailure(ApiConfig config, MonitoringResult result) {
        if (config.getConsecutiveFailures() >= failuresThreshold) {
            log.warn("API id={} name={} reached failure threshold ({}), sending alert",
                    config.getId(), config.getName(), failuresThreshold);
            sendAlert(config, result);
        }
    }

    public void handleRecovery(ApiConfig config, MonitoringResult result) {
        if (config.getConsecutiveFailures() == 1) {
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
}
