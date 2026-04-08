package com.konstroi.ksentinel.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "validation_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_config_id", nullable = false)
    private ApiConfig apiConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    // JSON_BODY rule
    private String jsonPath;
    private String operator;
    private String expectedValue;

    // LATENCY rule
    private Integer maxLatencyMs;

    // STATUS_CODE rule
    private Integer expectedStatus;

    private String description;

    public enum RuleType {
        STATUS_CODE,
        JSON_BODY,
        LATENCY
    }
}
