package com.konstroi.ksentinel.interfaces.rest.mapper;

import com.konstroi.ksentinel.domain.model.MonitoringResult;
import com.konstroi.ksentinel.interfaces.rest.dto.MonitoringResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonitoringResultMapper {

    @Mapping(target = "apiConfigId",   source = "apiConfig.id")
    @Mapping(target = "apiConfigName", source = "apiConfig.name")
    MonitoringResultResponse toResponse(MonitoringResult entity);
}
