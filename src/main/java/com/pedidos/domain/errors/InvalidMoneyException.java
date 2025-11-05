package com.pedidos.domain.errors;

public class InvalidMoneyException extends DomainException {
    public InvalidMoneyException(String message) {
        super(message);
    }

    public InvalidMoneyException(String message, Throwable cause) {
        super(message, cause);
    }
}
