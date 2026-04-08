package com.konstroi.ksentinel.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private long totalApis;
    private long enabledApis;
    private long downApis;
    private List<ApiConfigResponse> configs;
    private List<CompanyGroupResponse> companies;

    @Data
    @AllArgsConstructor
    public static class CompanyGroupResponse {
        private Long companyId;
        private String companyName;
        private long totalApis;
        private long enabledApis;
        private long downApis;
        private List<ApiConfigResponse> configs;
    }
}
