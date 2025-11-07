package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

class DeleteOrderNegativeTest {

    @Test
    void whenDeleteFailsReturnsInfraError() {
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
                return Result.fail(new InfraError("delete failed", null));
            }
        };

        EventBus bus = new EventBus() {
            @Override
            public Result<Void, AppError> publish(Object event) {
                return Result.ok(null);
            }
        };

        DeleteOrderUseCase uc = new DeleteOrderUseCase(repo, bus);
        Result<Void, com.pedidos.application.errors.AppError> res = uc.execute(OrderId.newId().toString());
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }

    @Test
    void whenEventBusFailsReturnsInfraError() {
        // repo deletes ok
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
                return Result.fail(new InfraError("bus fail", null));
            }
        };

        DeleteOrderUseCase uc = new DeleteOrderUseCase(repo, bus);
        Result<Void, com.pedidos.application.errors.AppError> res = uc.execute(OrderId.newId().toString());
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }
}
