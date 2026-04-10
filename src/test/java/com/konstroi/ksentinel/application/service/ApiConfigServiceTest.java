package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthType;
import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.model.HttpMethod;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.CompanyRepository;
import com.konstroi.ksentinel.exception.ApiConfigNotFoundException;
import com.konstroi.ksentinel.infrastructure.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiConfigServiceTest {

    @Mock ApiConfigRepository repository;
    @Mock CompanyRepository companyRepository;
    @Mock SchedulerService schedulerService;
    @Mock CurrentUserService currentUserService;

    @InjectMocks ApiConfigService service;

    private ApiConfig sampleConfig;
    private Company sampleCompany;

    @BeforeEach
    void setUp() {
        when(currentUserService.currentUserId()).thenReturn(10L);
        sampleCompany = Company.builder()
                .id(1L)
                .name("ACME")
                .build();
        sampleConfig = ApiConfig.builder()
                .id(1L)
                .name("Test API")
                .company(sampleCompany)
                .url("https://example.com/health")
                .httpMethod(HttpMethod.GET)
                .intervalSeconds(60)
                .timeoutSeconds(10)
                .enabled(true)
                .authType(AuthType.NONE)
                .consecutiveFailures(0)
                .build();
    }

    @Test
    void findAll_returnsAllConfigs() {
        when(repository.findAllWithDetailsByUserId(10L)).thenReturn(List.of(sampleConfig));

        List<ApiConfig> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test API");
    }

    @Test
    void findById_existingId_returnsConfig() {
        when(repository.findByIdWithDetailsAndUserId(1L, 10L)).thenReturn(Optional.of(sampleConfig));

        ApiConfig result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(repository.findByIdWithDetailsAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ApiConfigNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_enabledConfig_schedulesTask() {
        ApiConfig incoming = ApiConfig.builder()
                .name("New API")
                .company(sampleCompany)
                .url("https://api.example.com")
                .httpMethod(HttpMethod.GET)
                .intervalSeconds(30)
                .timeoutSeconds(5)
                .enabled(true)
                .authType(AuthType.NONE)
                .consecutiveFailures(0)
                .build();

        when(companyRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(sampleCompany));
        when(repository.save(any())).thenReturn(incoming.toBuilder().id(2L).build());

        ApiConfig result = service.create(incoming);

        assertThat(result.getId()).isEqualTo(2L);
        verify(schedulerService).schedule(any(ApiConfig.class));
    }

    @Test
    void create_disabledConfig_doesNotSchedule() {
        ApiConfig incoming = ApiConfig.builder()
                .name("Disabled API")
                .company(sampleCompany)
                .url("https://api.example.com")
                .httpMethod(HttpMethod.GET)
                .intervalSeconds(60)
                .timeoutSeconds(10)
                .enabled(false)
                .authType(AuthType.NONE)
                .consecutiveFailures(0)
                .build();

        when(companyRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(sampleCompany));
        when(repository.save(any())).thenReturn(incoming.toBuilder().id(3L).build());

        service.create(incoming);

        verify(schedulerService, never()).schedule(any());
    }

    @Test
    void delete_existingConfig_cancelsScheduleAndDeletes() {
        when(repository.findByIdWithDetailsAndUserId(1L, 10L)).thenReturn(Optional.of(sampleConfig));

        service.delete(1L);

        verify(schedulerService).cancel(1L);
        verify(repository).delete(sampleConfig);
    }

    @Test
    void toggleEnabled_enabledToDisabled_cancelsSchedule() {
        when(repository.findByIdWithDetailsAndUserId(1L, 10L)).thenReturn(Optional.of(sampleConfig));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApiConfig result = service.toggleEnabled(1L);

        assertThat(result.getEnabled()).isFalse();
        verify(schedulerService).cancel(1L);
    }
}
