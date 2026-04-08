package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.application.port.out.NotificationPort;
import com.konstroi.ksentinel.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock NotificationPort notificationPort;

    private AlertService alertService;

    private ApiConfig config;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(List.of(notificationPort));
        // inject threshold via reflection
        try {
            var field = AlertService.class.getDeclaredField("failuresThreshold");
            field.setAccessible(true);
            field.set(alertService, 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        config = ApiConfig.builder()
                .id(1L).name("Test").url("https://test.com")
                .httpMethod(HttpMethod.GET).intervalSeconds(60).timeoutSeconds(10)
                .enabled(true).authType(AuthType.NONE).consecutiveFailures(0)
                .build();
    }

    @Test
    void handleFailure_belowThreshold_doesNotNotify() {
        config.setConsecutiveFailures(2);
        MonitoringResult result = MonitoringResult.builder()
                .apiConfig(config).status(MonitoringStatus.DOWN).build();

        alertService.handleFailure(config, result);

        verify(notificationPort, never()).sendAlert(any(), any());
    }

    @Test
    void handleFailure_atThreshold_sendsAlert() {
        config.setConsecutiveFailures(3);
        MonitoringResult result = MonitoringResult.builder()
                .apiConfig(config).status(MonitoringStatus.DOWN).build();

        alertService.handleFailure(config, result);

        verify(notificationPort).sendAlert(config, result);
    }

    @Test
    void handleRecovery_firstSuccess_sendsRecoveryAlert() {
        config.setConsecutiveFailures(1);
        MonitoringResult result = MonitoringResult.builder()
                .apiConfig(config).status(MonitoringStatus.UP).build();

        alertService.handleRecovery(config, result);

        verify(notificationPort).sendAlert(config, result);
    }

    @Test
    void handleRecovery_normalSuccess_doesNotNotify() {
        config.setConsecutiveFailures(0);
        MonitoringResult result = MonitoringResult.builder()
                .apiConfig(config).status(MonitoringStatus.UP).build();

        alertService.handleRecovery(config, result);

        verify(notificationPort, never()).sendAlert(any(), any());
    }

    @Test
    void handleFailure_notificationThrows_doesNotPropagateException() {
        config.setConsecutiveFailures(5);
        MonitoringResult result = MonitoringResult.builder()
                .apiConfig(config).status(MonitoringStatus.DOWN).build();
        doThrow(new RuntimeException("SMTP failure")).when(notificationPort).sendAlert(any(), any());

        // should not throw
        alertService.handleFailure(config, result);
    }
}
