package com.example.learningplatform.common.exception;

/**
 * Конфликт с текущим состоянием данных: дубликат, запрет удаления и т.п.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
