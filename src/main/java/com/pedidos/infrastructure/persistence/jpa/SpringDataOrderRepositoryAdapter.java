package com.pedidos.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
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
 * Adapter (created by configuration when JPA is available).
 */
public class SpringDataOrderRepositoryAdapter implements OrderRepository {
    private final JpaOrderRepository jpa;

    public SpringDataOrderRepositoryAdapter(JpaOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Result<Void, AppError> save(Order order) {
        try {
            OrderEntity ent = toEntity(order);
            ent.setCreatedAt(Instant.now());
            jpa.save(ent);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(new InfraError("Failed to save order: " + e.getMessage(), e));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Optional<Order>, AppError> findById(com.pedidos.domain.valueobjects.OrderId id) {
        try {
            Optional<OrderEntity> ent = jpa.findById(id.getId().toString());
            return Result.ok(ent.map(this::toDomain));
        } catch (Exception e) {
            return Result.fail(new InfraError("Failed to query order: " + e.getMessage(), e));
        }
    }

    @Override
    @Transactional
    public Result<Void, AppError> delete(OrderId id) {
        try {
            jpa.deleteById(id.getId().toString());
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(new InfraError("Failed to delete order: " + e.getMessage(), e));
        }
    }

    private OrderEntity toEntity(Order o) {
        OrderEntity e = new OrderEntity();
        e.setId(o.getId().getId().toString());
        e.setCreatedAt(Instant.now());
        e.clearItems();
        for (OrderItem it : o.getItems()) {
            OrderItemEntity ie = new OrderItemEntity();
            ie.setProductId(it.getProductId().getId());
            ie.setQuantity(it.getQuantity().getValue());
            ie.setUnitAmount(it.getUnitPrice().getAmount());
            ie.setCurrency(it.getUnitPrice().getCurrency().getCode());
            e.addItem(ie);
        }
        return e;
    }

    private Order toDomain(OrderEntity e) {
        OrderId oid = new OrderId(UUID.fromString(e.getId()));
        Order order = Order.create(oid);
        for (OrderItemEntity it : e.getItems()) {
            ProductId pid = new ProductId(it.getProductId());
            Quantity q = new Quantity(it.getQuantity());
            Currency cur = Currency.of(it.getCurrency());
            Money m = new Money(it.getUnitAmount(), cur);
            order.addItem(new OrderItem(pid, q, m));
        }
        return order;
    }
}
