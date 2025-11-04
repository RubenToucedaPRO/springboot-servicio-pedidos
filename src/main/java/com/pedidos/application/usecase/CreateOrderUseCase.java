package com.pedidos.application.usecase;

import java.util.List;

import com.pedidos.application.dto.CreateOrderItemDto;
import com.pedidos.application.dto.CreateOrderRequestDto;
import com.pedidos.application.errors.AppError;
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
 * Caso de uso: crear un pedido.
 */
public final class CreateOrderUseCase {
    private final OrderRepository repository;
    private final EventBus eventBus;

    public CreateOrderUseCase(OrderRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    public Result<OrderId, AppError> execute(CreateOrderRequestDto request) {
        List<CreateOrderItemDto> items = request.getItems();
        if (items == null || items.isEmpty()) {
            return Result.fail(new ValidationError("Order must contain at least one item"));
        }

        OrderId orderId = OrderId.newId();
        Order order = Order.create(orderId);

        for (CreateOrderItemDto it : items) {
            if (it.quantity <= 0) {
                return Result.fail(new ValidationError("Quantity must be > 0 for product " + it.productId));
            }
            ProductId pid;
            try {
                pid = new ProductId(it.productId);
            } catch (Exception e) {
                return Result.fail(new ValidationError("Invalid product id: " + it.productId));
            }

            Currency currency;
            try {
                currency = Currency.of(it.currency);
            } catch (Exception e) {
                return Result.fail(new ValidationError("Unsupported currency: " + it.currency));
            }

            Money unitPrice;
            try {
                unitPrice = new Money(it.unitPrice, currency);
            } catch (Exception e) {
                return Result.fail(new ValidationError("Invalid unit price for product " + it.productId));
            }

            Quantity qty;
            try {
                qty = new Quantity(it.quantity);
            } catch (Exception e) {
                return Result.fail(new ValidationError("Invalid quantity for product " + it.productId));
            }

            order.addItem(new OrderItem(pid, qty, unitPrice));
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
