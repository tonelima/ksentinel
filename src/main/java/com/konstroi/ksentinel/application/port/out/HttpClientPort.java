package com.konstroi.ksentinel.application.port.out;

import com.konstroi.ksentinel.domain.model.ApiConfig;

public interface HttpClientPort {

    HttpResponse execute(ApiConfig config);

    record HttpResponse(
            int statusCode,
            String body,
            long latencyMs,
            String errorMessage
    ) {
        public boolean isError() {
            return errorMessage != null;
        }
    }
}
