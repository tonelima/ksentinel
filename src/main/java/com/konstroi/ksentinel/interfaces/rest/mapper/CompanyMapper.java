package com.konstroi.ksentinel.interfaces.rest.mapper;

import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.interfaces.rest.dto.CompanyRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.CompanyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "apiConfigs", ignore = true)
    @Mapping(target = "user", ignore = true)
    Company toEntity(CompanyRequest request);

    CompanyResponse toResponse(Company company);
}
