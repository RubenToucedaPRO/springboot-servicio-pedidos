package com.pedidos.infrastructure.persistence.jpa;

/**
 * Deprecated placeholder. The JPA entity was moved to
 * `com.pedidos.infrastructure.adapter.persistence.entity.OrderEntity`.
 *
 * This class is intentionally non-annotated to avoid duplicate JPA mapping
 * during the refactor. Do not use; it exists to keep old references compiling
 * until callers are migrated to the adapter package.
 */
@Deprecated
public class OrderEntity {
    private String id;
    private java.time.Instant createdAt;
    private java.util.Set<OrderItemEntity> items = new java.util.LinkedHashSet<>();

    public OrderEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.Instant createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.Set<OrderItemEntity> getItems() {
        return items;
    }

    public void setItems(java.util.Set<OrderItemEntity> items) {
        this.items = items;
    }

    public void addItem(OrderItemEntity it) {
        it.setOrder(this);
        items.add(it);
    }

    public void clearItems() {
        this.items.clear();
    }
}
