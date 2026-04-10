package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.MonitoringResultRepository;
import com.konstroi.ksentinel.exception.ApiConfigNotFoundException;
import com.konstroi.ksentinel.infrastructure.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MonitoringResultRepository resultRepository;
    private final ApiConfigRepository apiConfigRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public ApiMetrics getMetrics(Long configId, int lastHours) {
        assertConfigBelongsToCurrentUser(configId);
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours);

        long total = resultRepository.countByApiConfigIdSince(configId, since);
        long up = resultRepository.countByApiConfigIdAndStatusSince(configId, MonitoringStatus.UP, since);
        Double avgLatency = resultRepository.avgLatencyByApiConfigIdSince(configId, since);

        double uptimePercent = total == 0 ? 0.0 : (double) up / total * 100.0;

        return new ApiMetrics(
                configId,
                lastHours,
                total,
                up,
                total - up,
                Math.round(uptimePercent * 100.0) / 100.0,
                avgLatency != null ? Math.round(avgLatency) : 0
        );
    }

    private void assertConfigBelongsToCurrentUser(Long configId) {
        apiConfigRepository.findByIdWithDetailsAndUserId(configId, currentUserService.currentUserId())
                .orElseThrow(() -> new ApiConfigNotFoundException(configId));
    }

    public record ApiMetrics(
            Long configId,
            int periodHours,
            long totalChecks,
            long successCount,
            long failureCount,
            double uptimePercent,
            long avgLatencyMs
    ) {}
}
