package com.konstroi.ksentinel.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 1024)
    private String description;

    private List<@Email(message = "Invalid notification email") @Size(max = 255) String> notificationEmails;
}
