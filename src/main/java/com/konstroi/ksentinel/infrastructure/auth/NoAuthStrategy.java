package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;

@Component
public class NoAuthStrategy implements AuthStrategy {

    @Override
    public void applyAuth(HttpRequest.Builder requestBuilder, ApiConfig config) {
        // no-op
    }
}
