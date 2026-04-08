package com.konstroi.ksentinel.interfaces.rest.controller;

import com.konstroi.ksentinel.application.service.MonitoringService;
import com.konstroi.ksentinel.interfaces.rest.dto.MonitoringResultResponse;
import com.konstroi.ksentinel.interfaces.rest.mapper.MonitoringResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class MonitoringResultController {

    private final MonitoringService monitoringService;
    private final MonitoringResultMapper mapper;

    @GetMapping
    public Page<MonitoringResultResponse> findByConfigId(
            @RequestParam Long configId,
            @PageableDefault(size = 20) Pageable pageable) {
        return monitoringService.findByConfigId(configId, pageable).map(mapper::toResponse);
    }
}
