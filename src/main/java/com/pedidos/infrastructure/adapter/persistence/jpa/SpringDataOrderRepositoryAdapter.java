package com.pedidos.infrastructure.adapter.persistence.jpa;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.entities.Order;
import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.OrderItem;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.domain.valueobjects.Quantity;
import com.pedidos.infrastructure.adapter.persistence.entity.OrderEntity;
import com.pedidos.infrastructure.adapter.persistence.entity.OrderItemEntity;
import com.pedidos.shared.result.Result;

/**
 * Adapter (created by configuration when JPA is available).
 */
public class SpringDataOrderRepositoryAdapter implements OrderRepository {
    private final com.pedidos.infrastructure.adapter.persistence.jpa.JpaOrderRepository jpa;

    public SpringDataOrderRepositoryAdapter(com.pedidos.infrastructure.adapter.persistence.jpa.JpaOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Result<Void, AppError> save(Order order) {
        OrderEntity ent = toEntity(order);
        ent.setCreatedAt(Instant.now());
        jpa.save(ent); // allow exceptions to propagate so Spring can rollback correctly
        return Result.ok(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Optional<Order>, AppError> findById(com.pedidos.domain.valueobjects.OrderId id) {
        UUID uuid = Objects.requireNonNull(id.getId(), "order id is null");
        String idStr = Objects.requireNonNull(uuid.toString(), "order id string is null");
        Optional<OrderEntity> ent = jpa.findById(idStr);
        return Result.ok(ent.map(this::toDomain));

    }

    @Override
    @Transactional
    public Result<Void, AppError> delete(OrderId id) {
        UUID uuid = Objects.requireNonNull(id.getId(), "order id is null");
        String idStr = Objects.requireNonNull(uuid.toString(), "order id string is null");
        jpa.deleteById(idStr);
        return Result.ok(null);
    }

    @Override
    @Transactional
    public Result<Void, AppError> update(Order order) {
        Objects.requireNonNull(order, "order is null");
        UUID uuid = Objects.requireNonNull(order.getId().getId(), "order id is null");
        String idStr = Objects.requireNonNull(uuid.toString(), "order id string is null");

        Optional<OrderEntity> existing = jpa.findById(idStr);
        if (existing.isEmpty()) {
            return Result.fail(new com.pedidos.application.errors.NotFoundError("Order not found: " + idStr));
        }

        OrderEntity ent = existing.get();

        // Map existing items by productId for reuse
        Map<String, OrderItemEntity> existingByProduct = ent.getItems().stream()
                .collect(Collectors.toMap(OrderItemEntity::getProductId, e -> e, (a, b) -> a,
                        java.util.LinkedHashMap::new));

        Set<OrderItemEntity> targetItems = new LinkedHashSet<>();
        for (OrderItem it : order.getItems()) {
            String pid = Objects.requireNonNull(it.getProductId().getId(), "product id is null");
            OrderItemEntity ie = existingByProduct.remove(pid);
            if (ie == null) {
                ie = new OrderItemEntity();
                ie.setProductId(pid);
            }
            ie.setQuantity(it.getQuantity().getValue());
            ie.setUnitAmount(it.getUnitPrice().getAmount());
            ie.setCurrency(it.getUnitPrice().getCurrency().getCode());
            targetItems.add(ie);
        }

        // replace collection to let JPA handle orphanRemoval/cascade
        ent.clearItems();
        for (OrderItemEntity ie : targetItems) {
            ent.addItem(ie);
        }

        jpa.save(ent);
        return Result.ok(null);
    }

    private OrderEntity toEntity(Order o) {
        OrderEntity e = new OrderEntity();
        UUID uuid = Objects.requireNonNull(o.getId().getId(), "order id is null");
        String idStr = Objects.requireNonNull(uuid.toString(), "order id string is null");
        e.setId(idStr);
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
