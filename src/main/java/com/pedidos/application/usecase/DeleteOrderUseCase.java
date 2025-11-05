package com.pedidos.application.usecase;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.ValidationError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.events.OrderDeletedEvent;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

/**
 * Use case to delete an order by id and publish OrderDeletedEvent.
 */
public class DeleteOrderUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteOrderUseCase.class);
    private final OrderRepository repository;
    private final EventBus eventBus;

    public DeleteOrderUseCase(OrderRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    /**
     * Execute delete. Returns Result<Void, AppError>.
     */
    public Result<Void, AppError> execute(String orderId) {
        log.debug("DeleteOrderUseCase.execute - orderId={}", orderId);
        if (orderId == null || orderId.isBlank()) {
            log.warn("DeleteOrderUseCase - missing order id");
            return Result.fail(new ValidationError("Order id is required"));
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(orderId);
        } catch (IllegalArgumentException ex) {
            return Result.fail(new ValidationError("Invalid order id"));
        }

        OrderId oid = new OrderId(uuid);

        Result<Void, AppError> delRes = repository.delete(oid);
        if (delRes.isFail()) {
            log.error("DeleteOrderUseCase - failed to delete order {}: {}", oid, delRes.getError());
            return Result.fail(delRes.getError());
        }

        // Publish domain event about deletion. Include timestamp; reason currently
        // null.
        Result<Void, AppError> pub = eventBus.publish(new OrderDeletedEvent(oid, Instant.now(), null));
        if (pub.isFail()) {
            log.error("DeleteOrderUseCase - failed to publish OrderDeletedEvent for {}: {}", oid, pub.getError());
            // Bubble infra error from event bus
            return Result.fail(pub.getError());
        }

        log.info("DeleteOrderUseCase - order deleted {}", oid);

        return Result.ok(null);
    }
}
