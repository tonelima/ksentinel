package com.konstroi.ksentinel.infrastructure.auth;

import com.konstroi.ksentinel.domain.model.ApiConfig;

import java.net.http.HttpRequest;

public interface AuthStrategy {

    void applyAuth(HttpRequest.Builder requestBuilder, ApiConfig config);
}
