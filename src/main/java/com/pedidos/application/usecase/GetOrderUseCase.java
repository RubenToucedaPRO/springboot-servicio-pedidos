package com.pedidos.application.usecase;

import java.util.Optional;
import java.util.UUID;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.ValidationError;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.entities.Order;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

/**
 * Use case to retrieve an order by id.
 */
public class GetOrderUseCase {
    private final OrderRepository repository;

    public GetOrderUseCase(OrderRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute retrieval. Returns Result<Optional<Order>, AppError> so caller can
     * map 404.
     */
    public Result<Optional<Order>, AppError> execute(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return Result.fail(new ValidationError("Order id is required"));
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(orderId);
        } catch (IllegalArgumentException ex) {
            return Result.fail(new ValidationError("Invalid order id"));
        }

        OrderId oid = new OrderId(uuid);
        return repository.findById(oid);
    }
}
