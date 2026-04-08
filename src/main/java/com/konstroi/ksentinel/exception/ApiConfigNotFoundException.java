package com.konstroi.ksentinel.exception;

public class ApiConfigNotFoundException extends RuntimeException {
    public ApiConfigNotFoundException(Long id) {
        super("ApiConfig not found with id: " + id);
    }
}
