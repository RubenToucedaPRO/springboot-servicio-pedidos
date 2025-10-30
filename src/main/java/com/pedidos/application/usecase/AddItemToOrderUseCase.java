package com.pedidos.application.usecase;

import java.util.Optional;

import com.pedidos.application.dto.AddItemToOrderRequest;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.NotFoundError;
import com.pedidos.application.errors.ValidationError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.entities.Order;
import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.OrderItem;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.domain.valueobjects.Quantity;
import com.pedidos.shared.result.Result;

/**
 * Caso de uso: añadir un item a un pedido existente.
 */
public final class AddItemToOrderUseCase {
    private final OrderRepository repository;
    private final EventBus eventBus;

    public AddItemToOrderUseCase(OrderRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    public Result<OrderId, AppError> execute(AddItemToOrderRequest request) {
        OrderId orderId;
        try {
            orderId = new OrderId(java.util.UUID.fromString(request.orderId));
        } catch (Exception e) {
            return Result.fail(new ValidationError("Invalid order id: " + request.orderId));
        }

        Result<Optional<Order>, AppError> findRes = repository.findById(orderId);
        if (findRes.isFail())
            return Result.fail(findRes.getError());
        Optional<Order> maybe = findRes.getValue();
        if (maybe.isEmpty())
            return Result.fail(new NotFoundError("Order not found: " + orderId));

        Order order = maybe.get();

        if (request.quantity <= 0)
            return Result.fail(new ValidationError("Quantity must be > 0"));

        ProductId pid;
        try {
            pid = new ProductId(request.productId);
        } catch (Exception e) {
            return Result.fail(new ValidationError("Invalid product id: " + request.productId));
        }

        Currency currency;
        try {
            currency = Currency.of(request.currency);
        } catch (Exception e) {
            return Result.fail(new ValidationError("Unsupported currency: " + request.currency));
        }

        Money unitPrice;
        try {
            unitPrice = new Money(request.unitPrice, currency);
        } catch (Exception e) {
            return Result.fail(new ValidationError("Invalid unit price"));
        }

        Quantity qty;
        try {
            qty = new Quantity(request.quantity);
        } catch (Exception e) {
            return Result.fail(new ValidationError("Invalid quantity"));
        }

        order.addItem(new OrderItem(pid, qty, unitPrice));

        Result<Void, AppError> saveRes = repository.save(order);
        if (saveRes.isFail())
            return Result.fail(saveRes.getError());

        for (Object ev : order.pullDomainEvents()) {
            Result<Void, AppError> pub = eventBus.publish(ev);
            if (pub.isFail())
                return Result.fail(pub.getError());
        }

        return Result.ok(orderId);
    }
}
