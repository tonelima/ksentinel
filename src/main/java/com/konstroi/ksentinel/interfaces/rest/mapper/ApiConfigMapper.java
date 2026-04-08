package com.konstroi.ksentinel.interfaces.rest.mapper;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.domain.model.AuthCredential;
import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.model.ValidationRule;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigResponse;
import com.konstroi.ksentinel.interfaces.rest.dto.AuthCredentialRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.AuthCredentialResponse;
import com.konstroi.ksentinel.interfaces.rest.dto.ValidationRuleRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiConfigMapper {

    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "consecutiveFailures", ignore = true)
    @Mapping(target = "lastCheckedAt",       ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    @Mapping(target = "company",             source = "companyId")
    @Mapping(target = "credential",          source = "credential")
    @Mapping(target = "validationRules",     source = "validationRules")
    ApiConfig toEntity(ApiConfigRequest request);

    @Mapping(target = "validationRules", source = "validationRules")
    ApiConfigResponse toResponse(ApiConfig entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "apiConfig", ignore = true)
    @Mapping(target = "cachedToken",     ignore = true)
    @Mapping(target = "tokenExpiresAt",  ignore = true)
    AuthCredential toCredentialEntity(AuthCredentialRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "apiConfig", ignore = true)
    ValidationRule toRuleEntity(ValidationRuleRequest request);

    List<ValidationRule> toRuleEntities(List<ValidationRuleRequest> requests);

    @Mapping(target = "hasPassword", expression = "java(credential.getPassword() != null && !credential.getPassword().isBlank())")
    @Mapping(target = "hasToken", expression = "java(credential.getToken() != null && !credential.getToken().isBlank())")
    @Mapping(target = "hasApiKey", expression = "java(credential.getApiKey() != null && !credential.getApiKey().isBlank())")
    @Mapping(target = "hasClientSecret", expression = "java(credential.getClientSecret() != null && !credential.getClientSecret().isBlank())")
    AuthCredentialResponse toCredentialResponse(AuthCredential credential);

    @Mapping(target = "ruleType", expression = "java(rule.getRuleType().name())")
    ApiConfigResponse.ValidationRuleResponse toRuleResponse(ValidationRule rule);

    default Company mapCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return Company.builder().id(companyId).build();
    }

    default ApiConfigResponse.CompanySummaryResponse mapCompany(Company company) {
        if (company == null) {
            return null;
        }
        ApiConfigResponse.CompanySummaryResponse response = new ApiConfigResponse.CompanySummaryResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        return response;
    }
}
