package com.pedidos.application.errors;

public final record ValidationError(String message) implements AppError {
}
