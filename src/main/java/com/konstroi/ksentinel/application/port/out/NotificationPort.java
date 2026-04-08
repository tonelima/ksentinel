package com.konstroi.ksentinel.application.port.out;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;

public interface NotificationPort {

    void sendAlert(ApiConfig config, MonitoringResult result);

    boolean supports(NotificationType type);

    enum NotificationType {
        EMAIL, WEBHOOK, SLACK
    }
}
