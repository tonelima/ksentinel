package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.domain.model.AuthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AuthStrategyFactoryTest {

    private AuthStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AuthStrategyFactory(
                mock(NoAuthStrategy.class),
                mock(BasicAuthStrategy.class),
                mock(BearerTokenStrategy.class),
                mock(ApiKeyStrategy.class),
                mock(OAuth2Strategy.class)
        );
    }

    @ParameterizedTest
    @EnumSource(AuthType.class)
    void resolve_allAuthTypes_returnsNonNull(AuthType type) {
        AuthStrategy strategy = factory.resolve(type);
        assertThat(strategy).isNotNull();
    }

    @Test
    void resolve_none_returnsNoAuthStrategy() {
        assertThat(factory.resolve(AuthType.NONE)).isInstanceOf(NoAuthStrategy.class);
    }

    @Test
    void resolve_basic_returnsBasicAuthStrategy() {
        assertThat(factory.resolve(AuthType.BASIC)).isInstanceOf(BasicAuthStrategy.class);
    }

    @Test
    void resolve_bearer_returnsBearerStrategy() {
        assertThat(factory.resolve(AuthType.BEARER)).isInstanceOf(BearerTokenStrategy.class);
    }

    @Test
    void resolve_apiKey_returnsApiKeyStrategy() {
        assertThat(factory.resolve(AuthType.API_KEY)).isInstanceOf(ApiKeyStrategy.class);
    }

    @Test
    void resolve_oauth2_returnsOAuth2Strategy() {
        assertThat(factory.resolve(AuthType.OAUTH2)).isInstanceOf(OAuth2Strategy.class);
    }
}
