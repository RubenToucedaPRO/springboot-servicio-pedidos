package com.pedidos.application.port.out;

import java.util.Optional;

import com.pedidos.application.errors.AppError;
import com.pedidos.domain.entities.Order;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

/**
 * Outgoing port to persist and load orders.
 */
public interface OrderRepository {
    Result<Void, AppError> save(Order order);

    Result<Void, AppError> update(Order order);

    Result<Optional<Order>, AppError> findById(OrderId id);

    Result<Void, AppError> delete(OrderId id);
}
