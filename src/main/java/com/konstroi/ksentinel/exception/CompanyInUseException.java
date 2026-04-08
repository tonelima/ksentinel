package com.konstroi.ksentinel.exception;

public class CompanyInUseException extends RuntimeException {

    public CompanyInUseException(Long id) {
        super("Company with id=" + id + " cannot be deleted because it has monitored APIs");
    }
}
