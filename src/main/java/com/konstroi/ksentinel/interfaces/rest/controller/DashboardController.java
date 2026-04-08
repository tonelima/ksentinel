package com.konstroi.ksentinel.interfaces.rest.controller;

import com.konstroi.ksentinel.application.service.ApiConfigService;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigResponse;
import com.konstroi.ksentinel.interfaces.rest.dto.DashboardResponse;
import com.konstroi.ksentinel.interfaces.rest.mapper.ApiConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final ApiConfigService apiConfigService;
    private final ApiConfigMapper apiConfigMapper;

    @GetMapping({"/dashboard", "/api/dashboard"})
    public DashboardResponse getDashboard() {
        List<ApiConfig> configs = apiConfigService.findAll();
        List<ApiConfigResponse> configResponses = configs.stream()
                .map(apiConfigMapper::toResponse)
                .toList();

        long enabledApis = configs.stream()
                .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                .count();

        long downApis = configs.stream()
                .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                .filter(config -> config.getConsecutiveFailures() != null && config.getConsecutiveFailures() > 0)
                .count();

        Map<Long, List<ApiConfigResponse>> configsByCompany = configResponses.stream()
                .collect(Collectors.groupingBy(response -> response.getCompany().getId()));

        List<DashboardResponse.CompanyGroupResponse> companies = configs.stream()
                .collect(Collectors.groupingBy(config -> new CompanyGroupKey(
                        config.getCompany().getId(),
                        config.getCompany().getName()
                )))
                .entrySet().stream()
                .map(entry -> {
                    Long companyId = entry.getKey().companyId();
                    List<ApiConfig> companyConfigs = entry.getValue();
                    long companyEnabledApis = companyConfigs.stream()
                            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                            .count();
                    long companyDownApis = companyConfigs.stream()
                            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                            .filter(config -> config.getConsecutiveFailures() != null && config.getConsecutiveFailures() > 0)
                            .count();
                    return new DashboardResponse.CompanyGroupResponse(
                            companyId,
                            entry.getKey().companyName(),
                            companyConfigs.size(),
                            companyEnabledApis,
                            companyDownApis,
                            configsByCompany.get(companyId)
                    );
                })
                .sorted((left, right) -> left.getCompanyName().compareToIgnoreCase(right.getCompanyName()))
                .toList();

        return new DashboardResponse(
                configs.size(),
                enabledApis,
                downApis,
                configResponses,
                companies
        );
    }

    private record CompanyGroupKey(Long companyId, String companyName) {
    }
}
