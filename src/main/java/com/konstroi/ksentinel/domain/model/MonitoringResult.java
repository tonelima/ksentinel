package com.konstroi.ksentinel.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "monitoring_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_config_id", nullable = false)
    private ApiConfig apiConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitoringStatus status;

    private Integer httpStatus;

    private Long latencyMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime checkedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String validationDetails;

    public boolean isSuccess() {
        return status == MonitoringStatus.UP;
    }
}
