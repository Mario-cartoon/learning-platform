package com.example.learningplatform.common.exception;

/**
 * Нарушение бизнес-правила (не та роль, студент не записан на курс, просрочен дедлайн...).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
