package com.pedidos.infrastructure.eventbus;

import com.pedidos.application.errors.AppError;
import com.pedidos.shared.result.Result;

/**
 * Handler funcional para eventos del EventBus.
 */
@FunctionalInterface
public interface EventHandler<E> {
    Result<Void, AppError> handle(E event);
}