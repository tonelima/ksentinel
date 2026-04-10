package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.CompanyRepository;
import com.konstroi.ksentinel.exception.ApiConfigNotFoundException;
import com.konstroi.ksentinel.exception.CompanyNotFoundException;
import com.konstroi.ksentinel.infrastructure.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConfigService {

    private final ApiConfigRepository repository;
    private final CompanyRepository companyRepository;
    private final SchedulerService schedulerService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ApiConfig> findAll() {
        return repository.findAllWithDetailsByUserId(currentUserService.currentUserId());
    }

    @Transactional(readOnly = true)
    public List<ApiConfig> findAll(Long companyId) {
        Long userId = currentUserService.currentUserId();
        if (companyId == null) {
            return repository.findAllWithDetailsByUserId(userId);
        }
        return repository.findAllByCompanyIdAndCompanyUserId(companyId, userId).stream()
                .map(config -> repository.findByIdWithDetailsAndUserId(config.getId(), userId)
                        .orElseThrow(() -> new ApiConfigNotFoundException(config.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApiConfig findById(Long id) {
        return repository.findByIdWithDetailsAndUserId(id, currentUserService.currentUserId())
                .orElseThrow(() -> new ApiConfigNotFoundException(id));
    }

    @Transactional
    public ApiConfig create(ApiConfig config) {
        attachCompany(config);
        if (config.getCredential() != null) {
            config.getCredential().setApiConfig(config);
        }
        if (config.getValidationRules() != null) {
            config.getValidationRules().forEach(r -> r.setApiConfig(config));
        }
        ApiConfig saved = repository.save(config);
        log.info("Created ApiConfig id={} name={}", saved.getId(), saved.getName());
        if (Boolean.TRUE.equals(saved.getEnabled())) {
            schedulerService.schedule(saved);
        }
        return saved;
    }

    @Transactional
    public ApiConfig update(Long id, ApiConfig updated) {
        ApiConfig existing = findById(id);
        existing.setName(updated.getName());
        existing.setUrl(updated.getUrl());
        existing.setHttpMethod(updated.getHttpMethod());
        existing.setIntervalSeconds(updated.getIntervalSeconds());
        existing.setTimeoutSeconds(updated.getTimeoutSeconds());
        existing.setEnabled(updated.getEnabled());
        existing.setCompany(resolveCompany(updated.getCompany()));
        existing.setAuthType(updated.getAuthType());
        existing.setRequestHeaders(updated.getRequestHeaders());
        existing.setRequestBody(updated.getRequestBody());
        existing.setAlertEmail(updated.getAlertEmail());
        existing.setAlertWebhookUrl(updated.getAlertWebhookUrl());
        existing.setNotificationDelayMinutes(updated.getNotificationDelayMinutes());
        existing.setDescription(updated.getDescription());

        if (updated.getCredential() != null) {
            if (existing.getCredential() == null) {
                updated.getCredential().setApiConfig(existing);
                existing.setCredential(updated.getCredential());
            } else {
                mergeCredential(existing.getCredential(), updated.getCredential());
            }
        }

        existing.getValidationRules().clear();
        if (updated.getValidationRules() != null) {
            updated.getValidationRules().forEach(r -> r.setApiConfig(existing));
            existing.getValidationRules().addAll(updated.getValidationRules());
        }

        ApiConfig saved = repository.save(existing);
        schedulerService.reschedule(saved);
        log.info("Updated ApiConfig id={}", id);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        ApiConfig config = findById(id);
        schedulerService.cancel(id);
        repository.delete(config);
        log.info("Deleted ApiConfig id={}", id);
    }

    @Transactional
    public ApiConfig toggleEnabled(Long id) {
        ApiConfig config = findById(id);
        config.setEnabled(!config.getEnabled());
        ApiConfig saved = repository.save(config);
        if (Boolean.TRUE.equals(saved.getEnabled())) {
            schedulerService.schedule(saved);
        } else {
            schedulerService.cancel(id);
        }
        return saved;
    }

    private void mergeCredential(AuthCredential target, AuthCredential source) {
        target.setAuthType(source.getAuthType());
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
        target.setToken(source.getToken());
        target.setApiKey(source.getApiKey());
        target.setApiKeyHeader(source.getApiKeyHeader());
        target.setApiKeyAsQueryParam(source.getApiKeyAsQueryParam());
        target.setClientId(source.getClientId());
        target.setClientSecret(source.getClientSecret());
        target.setTokenUrl(source.getTokenUrl());
        target.setScope(source.getScope());
    }

    private void attachCompany(ApiConfig config) {
        config.setCompany(resolveCompany(config.getCompany()));
    }

    private Company resolveCompany(Company company) {
        if (company == null || company.getId() == null) {
            throw new IllegalArgumentException("Company is required");
        }
        return companyRepository.findByIdAndUserId(company.getId(), currentUserService.currentUserId())
                .orElseThrow(() -> new CompanyNotFoundException(company.getId()));
    }
}
