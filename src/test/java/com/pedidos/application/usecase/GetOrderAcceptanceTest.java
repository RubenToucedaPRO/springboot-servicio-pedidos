package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

class GetOrderAcceptanceTest {

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

    @Test
    void getOrderHappyPathReturnsOrder() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();

        com.pedidos.domain.entities.Order order = com.pedidos.domain.entities.Order.create(OrderId.newId());
        repo.save(order);

        GetOrderUseCase uc = new GetOrderUseCase(repo);
        Result<java.util.Optional<com.pedidos.domain.entities.Order>, com.pedidos.application.errors.AppError> res = uc
                .execute(order.getId().toString());

        assertTrue(res.isOk());
        assertTrue(res.getValue().isPresent());
        assertEquals(order.getId(), res.getValue().get().getId());
    }
}
