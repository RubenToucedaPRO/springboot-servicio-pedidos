package com.pedidos.application.usecase;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pedidos.application.dto.ItemToOrderDto;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.NotFoundError;
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
 * Caso de uso: añadir un item a un pedido existente.
 */
public final class AddItemToOrderUseCase {
    private static final Logger log = LoggerFactory.getLogger(AddItemToOrderUseCase.class);
    private final OrderRepository repository;
    private final EventBus eventBus;

    public AddItemToOrderUseCase(OrderRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    public Result<OrderId, AppError> execute(ItemToOrderDto request) {
        log.debug("AddItemToOrderUseCase.execute - orderId={} item={} ", request == null ? null : request.orderId,
                request == null ? null : request.item);
        if (request == null) {
            log.warn("AddItemToOrderUseCase - missing request");
            return Result.fail(new ValidationError("Missing request"));
        }

        OrderId orderId;
        try {
            orderId = new OrderId(java.util.UUID.fromString(request.orderId));
        } catch (Exception e) {
            return Result.fail(new ValidationError("Invalid order id: " + request.orderId));
        }

        Result<Optional<Order>, AppError> orderSearchResult = repository.findById(orderId);
        if (orderSearchResult.isFail()) {
            log.error("AddItemToOrderUseCase - repository.findById failed for {}: {}", orderId,
                    orderSearchResult.getError());
            return Result.fail(orderSearchResult.getError());
        }
        Optional<Order> optionalOrder = orderSearchResult.getValue();
        if (optionalOrder.isEmpty())
            return Result.fail(new NotFoundError("Order not found: " + orderId));

        Order order = optionalOrder.get();

        if (request.item == null) {
            return Result.fail(new ValidationError("Missing item"));
        }

        OrderItem orderItem;
        try {
            ProductId pid = new ProductId(request.item.productId);
            Quantity qty = new Quantity(request.item.quantity); // VO valida >0
            Currency cur = Currency.of(request.item.currency);
            Money price = new Money(request.item.unitPrice, cur);
            orderItem = new OrderItem(pid, qty, price);
            order.addItem(orderItem);
        } catch (IllegalArgumentException | DomainException e) {
            log.warn("AddItemToOrderUseCase - validation error: {}", e.getMessage());
            return Result.fail(new ValidationError(e.getMessage()));
        }

        Result<Void, AppError> saveRes = repository.save(order);
        if (saveRes.isFail()) {
            log.error("AddItemToOrderUseCase - failed to save order {}: {}", orderId, saveRes.getError());
            return Result.fail(saveRes.getError());
        }

        for (Object ev : order.pullDomainEvents()) {
            Result<Void, AppError> pub = eventBus.publish(ev);
            if (pub.isFail()) {
                log.error("AddItemToOrderUseCase - event publish failed for order {}: {}", orderId,
                        pub.getError());
                return Result.fail(pub.getError());
            }
        }

        log.info("AddItemToOrderUseCase - item added to order {}", orderId);

        return Result.ok(orderId);
    }
}
