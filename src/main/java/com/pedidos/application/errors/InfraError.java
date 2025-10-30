package com.pedidos.application.errors;

public final record InfraError(String message, Throwable cause) implements AppError {
}
