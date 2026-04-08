package com.konstroi.ksentinel.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_credential")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_config_id", nullable = false, unique = true)
    private ApiConfig apiConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType;

    // Basic auth
    private String username;
    private String password;

    // Bearer / OAuth2 access token
    private String token;

    // API Key
    private String apiKey;
    private String apiKeyHeader;
    private Boolean apiKeyAsQueryParam;

    // OAuth2 client credentials
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String scope;

    // OAuth2 token cache
    private String cachedToken;
    private LocalDateTime tokenExpiresAt;
}
