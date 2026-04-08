package com.konstroi.ksentinel.interfaces.rest.controller;

import com.konstroi.ksentinel.application.service.ApiConfigService;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigResponse;
import com.konstroi.ksentinel.interfaces.rest.mapper.ApiConfigMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@WithMockUser(roles = "ADMIN")
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ApiConfigService apiConfigService;

    @MockBean
    ApiConfigMapper apiConfigMapper;

    @Test
    void getDashboard_returnsAggregatedCountersAndConfigs() throws Exception {
        ApiConfig upApi = ApiConfig.builder()
                .id(1L)
                .name("API UP")
                .company(Company.builder().id(1L).name("ACME").build())
                .url("https://up.example.com")
                .enabled(true)
                .consecutiveFailures(0)
                .build();

        ApiConfig downApi = ApiConfig.builder()
                .id(2L)
                .name("API DOWN")
                .company(Company.builder().id(1L).name("ACME").build())
                .url("https://down.example.com")
                .enabled(true)
                .consecutiveFailures(3)
                .build();

        ApiConfig disabledApi = ApiConfig.builder()
                .id(3L)
                .name("API DISABLED")
                .company(Company.builder().id(2L).name("Globex").build())
                .url("https://disabled.example.com")
                .enabled(false)
                .consecutiveFailures(5)
                .build();

        ApiConfigResponse upResponse = new ApiConfigResponse();
        upResponse.setId(1L);
        upResponse.setName("API UP");
        upResponse.setCompany(companyResponse(1L, "ACME"));
        upResponse.setLastCheckedAt(LocalDateTime.of(2026, 4, 2, 13, 36, 50, 716_759_000));

        ApiConfigResponse downResponse = new ApiConfigResponse();
        downResponse.setId(2L);
        downResponse.setName("API DOWN");
        downResponse.setCompany(companyResponse(1L, "ACME"));

        ApiConfigResponse disabledResponse = new ApiConfigResponse();
        disabledResponse.setId(3L);
        disabledResponse.setName("API DISABLED");
        disabledResponse.setCompany(companyResponse(2L, "Globex"));

        when(apiConfigService.findAll()).thenReturn(List.of(upApi, downApi, disabledApi));
        when(apiConfigMapper.toResponse(upApi)).thenReturn(upResponse);
        when(apiConfigMapper.toResponse(downApi)).thenReturn(downResponse);
        when(apiConfigMapper.toResponse(disabledApi)).thenReturn(disabledResponse);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApis").value(3))
                .andExpect(jsonPath("$.enabledApis").value(2))
                .andExpect(jsonPath("$.downApis").value(1))
                .andExpect(jsonPath("$.configs[0].id").value(1))
                .andExpect(jsonPath("$.configs[0].lastCheckedAt").value("2026-04-02T13:36:50.716759"))
                .andExpect(jsonPath("$.companies[0].companyName").value("ACME"))
                .andExpect(jsonPath("$.companies[0].totalApis").value(2))
                .andExpect(jsonPath("$.configs[1].id").value(2))
                .andExpect(jsonPath("$.configs[2].id").value(3));
    }

    private ApiConfigResponse.CompanySummaryResponse companyResponse(Long id, String name) {
        ApiConfigResponse.CompanySummaryResponse response = new ApiConfigResponse.CompanySummaryResponse();
        response.setId(id);
        response.setName(name);
        return response;
    }
}
