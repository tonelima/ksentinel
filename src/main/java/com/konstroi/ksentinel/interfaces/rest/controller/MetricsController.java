package com.konstroi.ksentinel.interfaces.rest.controller;

import com.konstroi.ksentinel.application.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/api/metrics")
    public MetricsService.ApiMetrics getMetrics(
            @RequestParam Long configId,
            @RequestParam(defaultValue = "24") int periodHours) {
        return metricsService.getMetrics(configId, periodHours);
    }

    @GetMapping("/api/configs/{configId}/metrics")
    public MetricsService.ApiMetrics getMetricsByConfig(
            @PathVariable Long configId,
            @RequestParam(defaultValue = "24") int periodHours) {
        return metricsService.getMetrics(configId, periodHours);
    }
}
