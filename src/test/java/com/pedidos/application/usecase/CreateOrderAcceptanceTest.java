package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pedidos.application.dto.ItemDto;
import com.pedidos.application.dto.OrderDto;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.events.ItemAddedEvent;
import com.pedidos.domain.events.OrderCreatedEvent;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

class CreateOrderAcceptanceTest {

    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<com.pedidos.domain.valueobjects.OrderId, com.pedidos.domain.entities.Order> store = new HashMap<>();

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
        public Result<Optional<com.pedidos.domain.entities.Order>, AppError> findById(
                com.pedidos.domain.valueobjects.OrderId id) {
            return Result.ok(Optional.ofNullable(store.get(id)));
        }

        @Override
        public Result<Void, AppError> delete(com.pedidos.domain.valueobjects.OrderId id) {
            store.remove(id);
            return Result.ok(null);
        }

        public com.pedidos.domain.entities.Order getStored(OrderId id) {
            return store.get(id);
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
    void createOrderHappyPathPublishesEventsAndSaves() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        RecordingEventBus bus = new RecordingEventBus();

        CreateOrderUseCase uc = new CreateOrderUseCase(repo, bus);

        ItemDto it = new ItemDto("P-1", 2, BigDecimal.valueOf(3), "EUR");
        OrderDto dto = new OrderDto(List.of(it));

        Result<com.pedidos.domain.valueobjects.OrderId, com.pedidos.application.errors.AppError> res = uc.execute(dto);
        assertTrue(res.isOk());
        OrderId createdId = res.getValue();

        // repo should contain order
        com.pedidos.domain.entities.Order stored = repo.getStored(createdId);
        assertNotNull(stored);
        assertEquals(1, stored.getItems().size());

        // events should include OrderCreatedEvent and ItemAddedEvent
        boolean hasCreated = bus.published.stream().anyMatch(e -> e instanceof OrderCreatedEvent);
        boolean hasItemAdded = bus.published.stream().anyMatch(e -> e instanceof ItemAddedEvent);
        assertTrue(hasCreated);
        assertTrue(hasItemAdded);
    }
}
