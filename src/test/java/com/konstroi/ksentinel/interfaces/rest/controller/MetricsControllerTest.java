package com.konstroi.ksentinel.interfaces.rest.controller;

import com.konstroi.ksentinel.application.service.MetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
@WithMockUser(roles = "ADMIN")
class MetricsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MetricsService metricsService;

    @Test
    void getMetrics_returnsMetricsPayload() throws Exception {
        MetricsService.ApiMetrics metrics = new MetricsService.ApiMetrics(
                1L,
                24,
                100,
                98,
                2,
                98.0,
                120
        );

        when(metricsService.getMetrics(1L, 24)).thenReturn(metrics);

        mockMvc.perform(get("/api/metrics").param("configId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configId").value(1))
                .andExpect(jsonPath("$.periodHours").value(24))
                .andExpect(jsonPath("$.totalChecks").value(100))
                .andExpect(jsonPath("$.successCount").value(98))
                .andExpect(jsonPath("$.failureCount").value(2))
                .andExpect(jsonPath("$.uptimePercent").value(98.0))
                .andExpect(jsonPath("$.avgLatencyMs").value(120));
    }

    @Test
    void getMetricsByConfig_returnsMetricsPayload() throws Exception {
        MetricsService.ApiMetrics metrics = new MetricsService.ApiMetrics(
                2L,
                12,
                50,
                45,
                5,
                90.0,
                250
        );

        when(metricsService.getMetrics(2L, 12)).thenReturn(metrics);

        mockMvc.perform(get("/api/configs/2/metrics").param("periodHours", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configId").value(2))
                .andExpect(jsonPath("$.periodHours").value(12))
                .andExpect(jsonPath("$.totalChecks").value(50))
                .andExpect(jsonPath("$.successCount").value(45))
                .andExpect(jsonPath("$.failureCount").value(5))
                .andExpect(jsonPath("$.uptimePercent").value(90.0))
                .andExpect(jsonPath("$.avgLatencyMs").value(250));
    }
}
