package com.konstroi.ksentinel.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "api_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HttpMethod httpMethod = HttpMethod.GET;

    @Column(nullable = false)
    @Builder.Default
    private Integer intervalSeconds = 60;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 10;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthType authType = AuthType.NONE;

    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    private String alertEmail;

    @Column(length = 2048)
    private String alertWebhookUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer notificationDelayMinutes = 0;

    @Column(length = 1024)
    private String description;

    @Builder.Default
    private Integer consecutiveFailures = 0;

    private LocalDateTime lastCheckedAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToOne(mappedBy = "apiConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AuthCredential credential;

    @OneToMany(mappedBy = "apiConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ValidationRule> validationRules = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementFailures() {
        this.consecutiveFailures++;
        this.lastCheckedAt = LocalDateTime.now();
    }

    public void resetFailures() {
        this.consecutiveFailures = 0;
        this.lastCheckedAt = LocalDateTime.now();
    }
}
