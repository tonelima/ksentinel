package com.konstroi.ksentinel.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.konstroi.ksentinel.domain.model.MonitoringStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitoringResultResponse {
    private Long id;
    private Long apiConfigId;
    private String apiConfigName;
    private MonitoringStatus status;
    private Integer httpStatus;
    private Long latencyMs;
    private String errorMessage;
    private String validationDetails;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime checkedAt;
}
