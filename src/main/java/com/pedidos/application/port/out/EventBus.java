package com.pedidos.application.port.out;

import com.pedidos.application.errors.AppError;
import com.pedidos.shared.result.Result;

/**
 * Port to publish domain events to the outside world.
 */
public interface EventBus {
    Result<Void, AppError> publish(Object event);
}
