package com.pedidos.domain.errors;

public class InvalidProductIdException extends DomainException {
    public InvalidProductIdException(String message) {
        super(message);
    }

    public InvalidProductIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
