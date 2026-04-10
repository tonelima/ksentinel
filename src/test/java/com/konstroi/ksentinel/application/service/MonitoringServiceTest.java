package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.*;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.MonitoringResultRepository;
import com.konstroi.ksentinel.infrastructure.validation.ValidatorChain;
import com.konstroi.ksentinel.infrastructure.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock HttpClientPort httpClient;
    @Mock MonitoringResultRepository resultRepository;
    @Mock ApiConfigRepository configRepository;
    @Mock ValidatorChain validatorChain;
    @Mock AlertService alertService;

    @InjectMocks MonitoringService service;

    private ApiConfig config;

    @BeforeEach
    void setUp() {
        config = ApiConfig.builder()
                .id(1L)
                .name("Health Check")
                .url("https://example.com/health")
                .httpMethod(HttpMethod.GET)
                .intervalSeconds(60)
                .timeoutSeconds(10)
                .enabled(true)
                .authType(AuthType.NONE)
                .consecutiveFailures(0)
                .validationRules(List.of())
                .build();
    }

    @Test
    void check_successfulResponse_returnsUpStatus() {
        HttpClientPort.HttpResponse httpResp = new HttpClientPort.HttpResponse(200, "{\"status\":\"ok\"}", 120L, null);
        when(httpClient.execute(config)).thenReturn(httpResp);
        when(validatorChain.validate(any(), any())).thenReturn(ValidationResult.ok());
        when(resultRepository.save(any())).thenAnswer(inv -> {
            MonitoringResult r = inv.getArgument(0);
            r = MonitoringResult.builder()
                    .id(1L).apiConfig(config).status(r.getStatus())
                    .httpStatus(r.getHttpStatus()).latencyMs(r.getLatencyMs()).build();
            return r;
        });
        when(configRepository.save(any())).thenReturn(config);

        MonitoringResult result = service.check(config);

        assertThat(result.getStatus()).isEqualTo(MonitoringStatus.UP);
        assertThat(result.getHttpStatus()).isEqualTo(200);
        verify(alertService).handleRecovery(any(), any(), eq(0));
    }

    @Test
    void check_timeoutError_returnsTimeoutStatus() {
        HttpClientPort.HttpResponse httpResp = new HttpClientPort.HttpResponse(0, null, 10000L, "timeout: connection timed out");
        when(httpClient.execute(config)).thenReturn(httpResp);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(configRepository.save(any())).thenReturn(config);

        MonitoringResult result = service.check(config);

        assertThat(result.getStatus()).isEqualTo(MonitoringStatus.TIMEOUT);
        verify(alertService).handleFailure(any(), any(), eq(0));
    }

    @Test
    void check_connectionError_returnsDownStatus() {
        HttpClientPort.HttpResponse httpResp = new HttpClientPort.HttpResponse(0, null, 500L, "Connection refused");
        when(httpClient.execute(config)).thenReturn(httpResp);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(configRepository.save(any())).thenReturn(config);

        MonitoringResult result = service.check(config);

        assertThat(result.getStatus()).isEqualTo(MonitoringStatus.DOWN);
    }

    @Test
    void check_validationFails_returnsDownStatus() {
        HttpClientPort.HttpResponse httpResp = new HttpClientPort.HttpResponse(200, "{\"status\":\"degraded\"}", 100L, null);
        when(httpClient.execute(config)).thenReturn(httpResp);
        when(validatorChain.validate(any(), any())).thenReturn(ValidationResult.fail("Expected 'ok' but got 'degraded'"));
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(configRepository.save(any())).thenReturn(config);

        MonitoringResult result = service.check(config);

        assertThat(result.getStatus()).isEqualTo(MonitoringStatus.DOWN);
    }

    @Test
    void check_successAfterFailures_resetsConsecutiveFailures() {
        config.setConsecutiveFailures(3);
        HttpClientPort.HttpResponse httpResp = new HttpClientPort.HttpResponse(200, "{}", 100L, null);
        when(httpClient.execute(config)).thenReturn(httpResp);
        when(validatorChain.validate(any(), any())).thenReturn(ValidationResult.ok());
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(configRepository.save(any())).thenReturn(config);

        service.check(config);

        assertThat(config.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    void check_pingSuccessfulResponse_returnsUpStatus() {
        config.setHttpMethod(HttpMethod.PING);
        config.setUrl("192.168.0.1");
        HttpClientPort.HttpResponse httpResp = new HttpClientPort.HttpResponse(200, "PING OK", 15L, null);
        when(httpClient.execute(config)).thenReturn(httpResp);
        when(validatorChain.validate(any(), any())).thenReturn(ValidationResult.ok());
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(configRepository.save(any())).thenReturn(config);

        MonitoringResult result = service.check(config);

        assertThat(result.getStatus()).isEqualTo(MonitoringStatus.UP);
        assertThat(result.getHttpStatus()).isEqualTo(200);
        verify(alertService).handleRecovery(any(), any(), eq(0));
    }
}
