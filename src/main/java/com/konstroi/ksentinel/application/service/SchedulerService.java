package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final TaskScheduler taskScheduler;
    private final MonitoringService monitoringService;
    private final ApiConfigRepository apiConfigRepository;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSchedules() {
        log.info("Loading enabled API configs from database...");
        apiConfigRepository.findAllByEnabledTrue().forEach(this::schedule);
        log.info("Scheduled {} monitoring tasks", scheduledTasks.size());
    }

    public void schedule(ApiConfig config) {
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            log.debug("Skipping disabled ApiConfig id={}", config.getId());
            return;
        }
        cancel(config.getId());

        PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofSeconds(config.getIntervalSeconds()));
        trigger.setInitialDelay(Duration.ofSeconds(5));

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> runCheck(config.getId()),
                trigger
        );

        scheduledTasks.put(config.getId(), future);
        log.info("Scheduled monitoring for ApiConfig id={} every {}s", config.getId(), config.getIntervalSeconds());
    }

    public void reschedule(ApiConfig config) {
        log.info("Rescheduling ApiConfig id={}", config.getId());
        schedule(config);
    }

    public void cancel(Long configId) {
        ScheduledFuture<?> future = scheduledTasks.remove(configId);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled monitoring task for ApiConfig id={}", configId);
        }
    }

    public int activeTaskCount() {
        return scheduledTasks.size();
    }

    private void runCheck(Long configId) {
        try {
            apiConfigRepository.findByIdWithDetails(configId).ifPresentOrElse(
                    monitoringService::check,
                    () -> {
                        log.warn("ApiConfig id={} not found, cancelling task", configId);
                        cancel(configId);
                    }
            );
        } catch (Exception e) {
            log.error("Error during monitoring check for ApiConfig id={}", configId, e);
        }
    }
}
