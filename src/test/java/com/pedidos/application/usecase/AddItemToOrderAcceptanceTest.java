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
import com.pedidos.application.dto.ItemToOrderDto;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

class AddItemToOrderAcceptanceTest {

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
    void addItemToExistingOrderUpdatesRepoAndPublishes() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        RecordingEventBus bus = new RecordingEventBus();

        // create order and store
        com.pedidos.domain.entities.Order order = com.pedidos.domain.entities.Order.create(OrderId.newId());
        repo.save(order);

        AddItemToOrderUseCase uc = new AddItemToOrderUseCase(repo, bus);

        ItemDto item = new ItemDto("SKU-1", 3, BigDecimal.valueOf(2), "EUR");
        ItemToOrderDto req = new ItemToOrderDto(order.getId().toString(), item);

        Result<com.pedidos.domain.valueobjects.OrderId, com.pedidos.application.errors.AppError> res = uc.execute(req);
        assertTrue(res.isOk());

        com.pedidos.domain.entities.Order stored = repo.getStored(order.getId());
        assertNotNull(stored);
        assertEquals(1, stored.getItems().size());
        assertEquals(3, stored.getItems().get(0).getQuantity().getValue());

        // adding same product again merges quantities
        ItemDto item2 = new ItemDto("SKU-1", 2, BigDecimal.valueOf(2), "EUR");
        ItemToOrderDto req2 = new ItemToOrderDto(order.getId().toString(), item2);
        Result<com.pedidos.domain.valueobjects.OrderId, com.pedidos.application.errors.AppError> res2 = uc
                .execute(req2);
        assertTrue(res2.isOk());
        com.pedidos.domain.entities.Order stored2 = repo.getStored(order.getId());
        assertEquals(5, stored2.getItems().get(0).getQuantity().getValue());
    }
}
