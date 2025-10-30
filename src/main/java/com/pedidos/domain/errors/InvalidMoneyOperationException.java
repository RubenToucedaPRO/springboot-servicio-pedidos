package com.pedidos.domain.errors;

public class InvalidMoneyOperationException extends DomainException {
    public InvalidMoneyOperationException(String message) {
        super(message);
    }

    public InvalidMoneyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
