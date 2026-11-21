package com.example.learningplatform.common.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entityName, Long id) {
        this(String.format("%s с id=%d не существует", entityName, id));
    }
}
