package com.konstroi.ksentinel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Value("${monitoring.scheduler.thread-pool-size:10}")
    private int threadPoolSize;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(threadPoolSize);
        scheduler.setThreadNamePrefix("monitor-");
        scheduler.setErrorHandler(t ->
            org.slf4j.LoggerFactory.getLogger(SchedulerConfig.class)
                .error("Uncaught error in monitoring task", t)
        );
        scheduler.initialize();
        return scheduler;
    }
}
