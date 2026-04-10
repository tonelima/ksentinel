package com.konstroi.ksentinel.interfaces.rest.dto;

public record LoginResponse(String token, UserResponse user) {
}
