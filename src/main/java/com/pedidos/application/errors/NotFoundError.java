package com.pedidos.application.errors;

public final record NotFoundError(String message) implements AppError {
}
