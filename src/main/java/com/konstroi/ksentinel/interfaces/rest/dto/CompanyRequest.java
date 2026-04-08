package com.konstroi.ksentinel.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 1024)
    private String description;
}
