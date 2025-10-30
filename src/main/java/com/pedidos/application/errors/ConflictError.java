package com.pedidos.application.errors;

public final record ConflictError(String message) implements AppError {
}
