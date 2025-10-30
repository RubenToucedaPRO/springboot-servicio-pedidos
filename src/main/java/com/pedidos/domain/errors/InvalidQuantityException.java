package com.pedidos.domain.errors;

public class InvalidQuantityException extends DomainException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
