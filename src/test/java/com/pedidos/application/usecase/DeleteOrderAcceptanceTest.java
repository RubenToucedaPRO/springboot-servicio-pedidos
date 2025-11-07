package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.events.OrderDeletedEvent;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

class DeleteOrderAcceptanceTest {

    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<OrderId, com.pedidos.domain.entities.Order> store = new HashMap<>();

        @Override
        public Result<Void, AppError> save(com.pedidos.domain.entities.Order order) {
            store.put(order.getId(), order);
            return Result.ok(null);
        }

        @Override
        public Result<Void, AppError> update(com.pedidos.domain.entities.Order order) {
            store.put(order.getId(), order);
            return Result.ok(null);
        }

        @Override
        public Result<Optional<com.pedidos.domain.entities.Order>, AppError> findById(OrderId id) {
            return Result.ok(Optional.ofNullable(store.get(id)));
        }

        @Override
        public Result<Void, AppError> delete(OrderId id) {
            store.remove(id);
            return Result.ok(null);
        }
    }

    static class RecordingEventBus implements EventBus {
        final List<Object> published = new ArrayList<>();

        @Override
        public Result<Void, AppError> publish(Object event) {
            published.add(event);
            return Result.ok(null);
        }
    }

    @Test
    void deleteOrderRemovesAndPublishesEvent() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        RecordingEventBus bus = new RecordingEventBus();

        com.pedidos.domain.entities.Order order = com.pedidos.domain.entities.Order.create(OrderId.newId());
        repo.save(order);

        DeleteOrderUseCase uc = new DeleteOrderUseCase(repo, bus);
        Result<Void, com.pedidos.application.errors.AppError> res = uc.execute(order.getId().toString());

        assertTrue(res.isOk());
        // verify removed
        Result<java.util.Optional<com.pedidos.domain.entities.Order>, com.pedidos.application.errors.AppError> findRes = repo
                .findById(order.getId());
        assertTrue(findRes.isOk());
        assertTrue(findRes.getValue().isEmpty());

        // verify event published
        boolean hasDeleted = bus.published.stream().anyMatch(e -> e instanceof OrderDeletedEvent);
        assertTrue(hasDeleted);
    }
}
