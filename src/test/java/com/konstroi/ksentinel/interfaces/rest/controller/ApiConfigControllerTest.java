package com.konstroi.ksentinel.interfaces.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konstroi.ksentinel.application.service.ApiConfigService;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.domain.model.AuthType;
import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.model.HttpMethod;
import com.konstroi.ksentinel.exception.ApiConfigNotFoundException;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigRequest;
import com.konstroi.ksentinel.interfaces.rest.mapper.ApiConfigMapper;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigResponse;
import com.konstroi.ksentinel.interfaces.rest.dto.AuthCredentialResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiConfigController.class)
@WithMockUser(roles = "ADMIN")
class ApiConfigControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ApiConfigService service;
    @MockBean ApiConfigMapper mapper;

    private ApiConfig sampleConfig;
    private ApiConfigResponse sampleResponse;
    private Company sampleCompany;

    @BeforeEach
    void setUp() {
        sampleCompany = Company.builder()
                .id(1L)
                .name("ACME")
                .build();
        sampleConfig = ApiConfig.builder()
                .id(1L).name("Test API").company(sampleCompany).url("https://example.com/health")
                .httpMethod(HttpMethod.GET).intervalSeconds(60).timeoutSeconds(10)
                .enabled(true).authType(AuthType.NONE).consecutiveFailures(0)
                .build();

        AuthCredential credential = AuthCredential.builder()
                .id(10L)
                .authType(AuthType.BASIC)
                .username("monitor-user")
                .password("secret")
                .build();
        sampleConfig.setCredential(credential);

        sampleResponse = new ApiConfigResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Test API");
        ApiConfigResponse.CompanySummaryResponse companyResponse = new ApiConfigResponse.CompanySummaryResponse();
        companyResponse.setId(1L);
        companyResponse.setName("ACME");
        sampleResponse.setCompany(companyResponse);
        sampleResponse.setUrl("https://example.com/health");
        sampleResponse.setEnabled(true);
        sampleResponse.setAuthType(AuthType.NONE);
        sampleResponse.setIntervalSeconds(60);
        AuthCredentialResponse credentialResponse = new AuthCredentialResponse();
        credentialResponse.setId(10L);
        credentialResponse.setAuthType(AuthType.BASIC);
        credentialResponse.setUsername("monitor-user");
        credentialResponse.setHasPassword(true);
        sampleResponse.setCredential(credentialResponse);
    }

    @Test
    void findAll_returnsListOfConfigs() throws Exception {
        when(service.findAll(null)).thenReturn(List.of(sampleConfig));
        when(mapper.toResponse(sampleConfig)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test API"));
    }

    @Test
    void findById_existingId_returnsConfig() throws Exception {
        when(service.findById(1L)).thenReturn(sampleConfig);
        when(mapper.toResponse(sampleConfig)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/configs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test API"))
                .andExpect(jsonPath("$.credential.id").value(10))
                .andExpect(jsonPath("$.credential.authType").value("BASIC"))
                .andExpect(jsonPath("$.credential.username").value("monitor-user"))
                .andExpect(jsonPath("$.credential.hasPassword").value(true))
                .andExpect(jsonPath("$.credential.password").doesNotExist());
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        when(service.findById(99L)).thenThrow(new ApiConfigNotFoundException(99L));

        mockMvc.perform(get("/api/configs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        ApiConfigRequest request = new ApiConfigRequest();
        request.setName("New API");
        request.setCompanyId(1L);
        request.setUrl("https://newapi.com/health");
        request.setIntervalSeconds(30);
        request.setTimeoutSeconds(5);
        request.setAuthType(AuthType.NONE);

        when(mapper.toEntity(any())).thenReturn(sampleConfig);
        when(service.create(any())).thenReturn(sampleConfig);
        when(mapper.toResponse(sampleConfig)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/configs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_invalidRequest_returns400() throws Exception {
        ApiConfigRequest request = new ApiConfigRequest();
        // name and url are blank - should fail validation

        mockMvc.perform(post("/api/configs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/configs/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void toggle_returnsUpdatedConfig() throws Exception {
        ApiConfigResponse toggled = new ApiConfigResponse();
        toggled.setId(1L);
        toggled.setEnabled(false);

        when(service.toggleEnabled(1L)).thenReturn(sampleConfig);
        when(mapper.toResponse(sampleConfig)).thenReturn(toggled);

        mockMvc.perform(patch("/api/configs/1/toggle").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void create_pingRequest_acceptsPingMethod() throws Exception {
        ApiConfigRequest request = new ApiConfigRequest();
        request.setName("Ping IP");
        request.setCompanyId(1L);
        request.setUrl("192.168.0.1");
        request.setHttpMethod(HttpMethod.PING);
        request.setIntervalSeconds(30);
        request.setTimeoutSeconds(5);
        request.setAuthType(AuthType.NONE);

        ApiConfig pingConfig = ApiConfig.builder()
                .id(2L)
                .name("Ping IP")
                .company(sampleCompany)
                .url("192.168.0.1")
                .httpMethod(HttpMethod.PING)
                .intervalSeconds(30)
                .timeoutSeconds(5)
                .enabled(true)
                .authType(AuthType.NONE)
                .consecutiveFailures(0)
                .build();

        ApiConfigResponse pingResponse = new ApiConfigResponse();
        pingResponse.setId(2L);
        pingResponse.setName("Ping IP");
        pingResponse.setCompany(companyResponse(1L, "ACME"));
        pingResponse.setUrl("192.168.0.1");
        pingResponse.setHttpMethod(HttpMethod.PING);

        when(mapper.toEntity(any())).thenReturn(pingConfig);
        when(service.create(any())).thenReturn(pingConfig);
        when(mapper.toResponse(pingConfig)).thenReturn(pingResponse);

        mockMvc.perform(post("/api/configs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpMethod").value("PING"));
    }

    private ApiConfigResponse.CompanySummaryResponse companyResponse(Long id, String name) {
        ApiConfigResponse.CompanySummaryResponse response = new ApiConfigResponse.CompanySummaryResponse();
        response.setId(id);
        response.setName(name);
        return response;
    }
}
