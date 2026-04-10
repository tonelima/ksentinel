package com.konstroi.ksentinel.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SmtpSettingsRequest {

    @Size(max = 255)
    private String host;

    @Min(1)
    @Max(65535)
    private Integer port;

    @Size(max = 255)
    private String username;

    @Size(max = 1024)
    private String password;

    @Email(message = "Invalid SMTP from email")
    @Size(max = 255)
    private String fromEmail;

    private Boolean auth = true;
    private Boolean starttls = true;
}
