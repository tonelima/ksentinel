package com.konstroi.ksentinel.exception;

public class DuplicateCompanyException extends RuntimeException {

    public DuplicateCompanyException(String name) {
        super("Company with name='" + name + "' already exists");
    }
}
