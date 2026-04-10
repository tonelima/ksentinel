package com.konstroi.ksentinel.interfaces.rest.dto;

public record SmtpSettingsResponse(
        String host,
        Integer port,
        String username,
        String fromEmail,
        Boolean auth,
        Boolean starttls,
        Boolean configured
) {
}
