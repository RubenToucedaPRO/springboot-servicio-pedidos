package com.pedidos.domain.errors;

public class InvalidCurrencyException extends DomainException {
    public InvalidCurrencyException(String message) {
        super(message);
    }

    public InvalidCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
