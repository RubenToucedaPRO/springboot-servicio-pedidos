package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pedidos.application.dto.ItemDto;
import com.pedidos.application.dto.ItemToOrderDto;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.errors.NotFoundError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.shared.result.Result;

class AddItemToOrderNegativeTest {

    @Test
    void whenFindByIdFailsUseCaseReturnsInfraError() {
        OrderRepository repo = new OrderRepository() {
            @Override
            public Result<Void, AppError> save(com.pedidos.domain.entities.Order order) {
                return Result.ok(null);
            }

            @Override
            public Result<Void, AppError> update(com.pedidos.domain.entities.Order order) {
                return Result.ok(null);
            }

            @Override
            public Result<Optional<com.pedidos.domain.entities.Order>, AppError> findById(
                    com.pedidos.domain.valueobjects.OrderId id) {
                return Result.fail(new InfraError("db error", null));
            }

            @Override
            public Result<Void, AppError> delete(com.pedidos.domain.valueobjects.OrderId id) {
                return Result.ok(null);
            }
        };

        EventBus bus = new EventBus() {
            @Override
            public Result<Void, AppError> publish(Object event) {
                return Result.ok(null);
            }
        };

        AddItemToOrderUseCase uc = new AddItemToOrderUseCase(repo, bus);
        ItemDto item = new ItemDto("SKU", 1, BigDecimal.valueOf(1), "EUR");
        ItemToOrderDto req = new ItemToOrderDto(com.pedidos.domain.valueobjects.OrderId.newId().toString(), item);

        Result<com.pedidos.domain.valueobjects.OrderId, AppError> res = uc.execute(req);
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }

    @Test
    void whenOrderNotFoundReturnsNotFoundError() {
        OrderRepository repo = new OrderRepository() {
            @Override
            public Result<Void, AppError> save(com.pedidos.domain.entities.Order order) {
                return Result.ok(null);
            }

            @Override
            public Result<Void, AppError> update(com.pedidos.domain.entities.Order order) {
                return Result.ok(null);
            }

            @Override
            public Result<Optional<com.pedidos.domain.entities.Order>, AppError> findById(
                    com.pedidos.domain.valueobjects.OrderId id) {
                return Result.ok(Optional.empty());
            }

            @Override
            public Result<Void, AppError> delete(com.pedidos.domain.valueobjects.OrderId id) {
                return Result.ok(null);
            }
        };

        EventBus bus = new EventBus() {
            @Override
            public Result<Void, AppError> publish(Object event) {
                return Result.ok(null);
            }
        };

        AddItemToOrderUseCase uc = new AddItemToOrderUseCase(repo, bus);
        ItemDto item = new ItemDto("SKU", 1, BigDecimal.valueOf(1), "EUR");
        ItemToOrderDto req = new ItemToOrderDto(java.util.UUID.randomUUID().toString(), item);

        Result<com.pedidos.domain.valueobjects.OrderId, AppError> res = uc.execute(req);
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof NotFoundError);
    }

    @Test
    void whenUpdateFailsReturnsInfraError() {
        // prepare repo that finds order ok but update fails
        com.pedidos.domain.entities.Order order = com.pedidos.domain.entities.Order
                .create(com.pedidos.domain.valueobjects.OrderId.newId());
        OrderRepository repo = new OrderRepository() {
            @Override
            public Result<Void, AppError> save(com.pedidos.domain.entities.Order o) {
                return Result.ok(null);
            }

            @Override
            public Result<Void, AppError> update(com.pedidos.domain.entities.Order o) {
                return Result.fail(new InfraError("update failed", null));
            }

            @Override
            public Result<Optional<com.pedidos.domain.entities.Order>, AppError> findById(
                    com.pedidos.domain.valueobjects.OrderId id) {
                return Result.ok(Optional.of(order));
            }

            @Override
            public Result<Void, AppError> delete(com.pedidos.domain.valueobjects.OrderId id) {
                return Result.ok(null);
            }
        };

        EventBus bus = new EventBus() {
            @Override
            public Result<Void, AppError> publish(Object event) {
                return Result.ok(null);
            }
        };

        AddItemToOrderUseCase uc = new AddItemToOrderUseCase(repo, bus);
        ItemDto item = new ItemDto("SKU", 1, BigDecimal.valueOf(1), "EUR");
        ItemToOrderDto req = new ItemToOrderDto(order.getId().toString(), item);

        Result<com.pedidos.domain.valueobjects.OrderId, AppError> res = uc.execute(req);
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }
}
