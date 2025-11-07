package com.pedidos.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.errors.ValidationError;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.shared.result.Result;

class GetOrderNegativeTest {

    @Test
    void invalidUuidReturnsValidationError() {
        GetOrderUseCase uc = new GetOrderUseCase(new OrderRepository() {
            @Override
            public Result<Void, AppError> save(com.pedidos.domain.entities.Order order) {
                return Result.ok(null);
            }

            @Override
            public Result<Void, AppError> update(com.pedidos.domain.entities.Order order) {
                return Result.ok(null);
            }

            @Override
            public Result<java.util.Optional<com.pedidos.domain.entities.Order>, AppError> findById(OrderId id) {
                return Result.ok(Optional.empty());
            }

            @Override
            public Result<Void, AppError> delete(OrderId id) {
                return Result.ok(null);
            }
        });

        Result<java.util.Optional<com.pedidos.domain.entities.Order>, com.pedidos.application.errors.AppError> res = uc
                .execute("not-a-uuid");
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof ValidationError);
    }

    @Test
    void repositoryFailureIsPropagatedAsInfraError() {
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
            public Result<java.util.Optional<com.pedidos.domain.entities.Order>, AppError> findById(OrderId id) {
                return Result.fail(new InfraError("db fail", null));
            }

            @Override
            public Result<Void, AppError> delete(OrderId id) {
                return Result.ok(null);
            }
        };

        GetOrderUseCase uc = new GetOrderUseCase(repo);
        Result<java.util.Optional<com.pedidos.domain.entities.Order>, com.pedidos.application.errors.AppError> res = uc
                .execute(OrderId.newId().toString());
        assertTrue(res.isFail());
        assertTrue(res.getError() instanceof InfraError);
    }
}
