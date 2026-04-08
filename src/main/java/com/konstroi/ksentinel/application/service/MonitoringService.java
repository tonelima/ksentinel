package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.application.port.out.HttpClientPort;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.MonitoringResult;
import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.MonitoringResultRepository;
import com.konstroi.ksentinel.infrastructure.validation.ValidatorChain;
import com.konstroi.ksentinel.infrastructure.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final HttpClientPort httpClient;
    private final MonitoringResultRepository resultRepository;
    private final ApiConfigRepository configRepository;
    private final ValidatorChain validatorChain;
    private final AlertService alertService;

    @Transactional
    public MonitoringResult check(ApiConfig config) {
        log.debug("Checking API id={} name={} url={}", config.getId(), config.getName(), config.getUrl());

        HttpClientPort.HttpResponse response = httpClient.execute(config);

        MonitoringResult result = buildResult(config, response);
        MonitoringResult saved = resultRepository.save(result);

        updateConfigStatus(config, saved);

        if (!saved.isSuccess()) {
            alertService.handleFailure(config, saved);
        } else {
            alertService.handleRecovery(config, saved);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<MonitoringResult> findByConfigId(Long configId, Pageable pageable) {
        return resultRepository.findByApiConfigIdOrderByCheckedAtDesc(configId, pageable);
    }

    private MonitoringResult buildResult(ApiConfig config, HttpClientPort.HttpResponse response) {
        if (response.isError()) {
            MonitoringStatus status = response.errorMessage().contains("timeout")
                    ? MonitoringStatus.TIMEOUT
                    : MonitoringStatus.DOWN;
            return MonitoringResult.builder()
                    .apiConfig(config)
                    .status(status)
                    .latencyMs(response.latencyMs())
                    .errorMessage(response.errorMessage())
                    .build();
        }

        ValidationResult validation = validatorChain.validate(response, config.getValidationRules());

        MonitoringStatus status;
        if (!validation.passed()) {
            status = MonitoringStatus.DOWN;
        } else if (response.latencyMs() > config.getTimeoutSeconds() * 1000L * 0.8) {
            status = MonitoringStatus.DEGRADED;
        } else {
            status = MonitoringStatus.UP;
        }

        return MonitoringResult.builder()
                .apiConfig(config)
                .status(status)
                .httpStatus(response.statusCode())
                .latencyMs(response.latencyMs())
                .responseBody(truncate(response.body(), 2000))
                .validationDetails(validation.details())
                .build();
    }

    private void updateConfigStatus(ApiConfig config, MonitoringResult result) {
        if (result.isSuccess()) {
            config.resetFailures();
        } else {
            config.incrementFailures();
        }
        configRepository.save(config);
    }

    private String truncate(String text, int max) {
        if (text == null) return null;
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }
}
