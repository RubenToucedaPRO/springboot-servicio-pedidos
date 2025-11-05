package com.pedidos.application.usecase;

import java.util.List;

import com.pedidos.application.dto.ItemDto;
import com.pedidos.application.dto.OrderDto;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.ValidationError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.entities.Order;
import com.pedidos.domain.errors.DomainException;
import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.OrderItem;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.domain.valueobjects.Quantity;
import com.pedidos.shared.result.Result;

/**
 * Caso de uso: crear un pedido.
 */
public final class CreateOrderUseCase {
    private final OrderRepository repository;
    private final EventBus eventBus;

    public CreateOrderUseCase(OrderRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    public Result<OrderId, AppError> execute(OrderDto request) {
        List<ItemDto> items = request.getItems();
        if (items == null || items.isEmpty()) {
            return Result.fail(new ValidationError("Order must contain at least one item"));
        }

        OrderId orderId = OrderId.newId();
        Order order = Order.create(orderId);

        for (ItemDto it : items) {
            if (it.quantity == null) {
                return Result.fail(new ValidationError("Quantity is required for product " + it.productId));
            }
            ProductId pid;
            try {
                pid = new ProductId(it.productId);
                Currency currency = Currency.of(it.currency);
                Money unitPrice = new Money(it.unitPrice, currency);
                Quantity qty = new Quantity(it.quantity);

                order.addItem(new OrderItem(pid, qty, unitPrice));
            } catch (IllegalArgumentException | DomainException e) {
                return Result.fail(new ValidationError(e.getMessage()));
            }
        }

        // persist
        Result<Void, AppError> saveRes = repository.save(order);
        if (saveRes.isFail())
            return Result.fail(saveRes.getError());

        // publish events
        List<Object> events = order.pullDomainEvents();
        for (Object ev : events) {
            Result<Void, AppError> pub = eventBus.publish(ev);
            if (pub.isFail())
                return Result.fail(pub.getError());
        }

        return Result.ok(orderId);
    }
}
