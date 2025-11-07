package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pedidos.application.dto.ItemDto;
import com.pedidos.application.dto.OrderDto;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.shared.result.Result;

class CreateOrderNegativeTest {

    static class FailingSaveRepository implements OrderRepository {
        @Override
        public Result<Void, AppError> save(com.pedidos.domain.entities.Order order) {
            return Result.fail(new InfraError("db error", new RuntimeException("boom")));
        }

        @Override
        public Result<Void, AppError> update(com.pedidos.domain.entities.Order order) {
            return Result.fail(new InfraError("not implemented", null));
        }

        @Override
        public Result<java.util.Optional<com.pedidos.domain.entities.Order>, AppError> findById(
                com.pedidos.domain.valueobjects.OrderId id) {
            return Result.ok(java.util.Optional.empty());
        }

        @Override
        public Result<Void, AppError> delete(com.pedidos.domain.valueobjects.OrderId id) {
            return Result.fail(new InfraError("not implemented", null));
        }
    }

    static class EventBusFails implements EventBus {
        @Override
        public Result<Void, AppError> publish(Object event) {
            return Result.fail(new InfraError("event bus down", null));
        }
    }

    @Test
    void createOrderWhenSaveFailsReturnsInfraError() {
        OrderRepository repo = new FailingSaveRepository();
        EventBus bus = new RecordingEventBus();
        CreateOrderUseCase uc = new CreateOrderUseCase(repo, bus);

        ItemDto it = new ItemDto("P-1", 1, BigDecimal.valueOf(1), "EUR");
        OrderDto dto = new OrderDto(List.of(it));

        Result<com.pedidos.domain.valueobjects.OrderId, AppError> res = uc.execute(dto);
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }

    @Test
    void createOrderWhenEventBusFailsReturnsInfraError() {
        // repository saves ok but event bus fails
        OrderRepository repo = new InMemoryOkRepository();
        EventBus bus = new EventBusFails();
        CreateOrderUseCase uc = new CreateOrderUseCase(repo, bus);

        ItemDto it = new ItemDto("P-1", 1, BigDecimal.valueOf(1), "EUR");
        OrderDto dto = new OrderDto(List.of(it));

        Result<com.pedidos.domain.valueobjects.OrderId, AppError> res = uc.execute(dto);
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }

    // small helpers reused from previous tests
    static class InMemoryOkRepository implements OrderRepository {
        @Override
        public Result<Void, AppError> save(com.pedidos.domain.entities.Order order) {
            return Result.ok(null);
        }

        @Override
        public Result<Void, AppError> update(com.pedidos.domain.entities.Order order) {
            return Result.ok(null);
        }

        @Override
        public Result<java.util.Optional<com.pedidos.domain.entities.Order>, AppError> findById(
                com.pedidos.domain.valueobjects.OrderId id) {
            return Result.ok(java.util.Optional.empty());
        }

        @Override
        public Result<Void, AppError> delete(com.pedidos.domain.valueobjects.OrderId id) {
            return Result.ok(null);
        }
    }

    static class RecordingEventBus implements EventBus {
        @Override
        public Result<Void, AppError> publish(Object event) {
            return Result.ok(null);
        }
    }
}
