package com.pedidos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing a line in an order: product, quantity and unit
 * price.
 */
public final class OrderItem {
    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;

    public OrderItem(ProductId productId, Quantity quantity, Money unitPrice) {
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.quantity = Objects.requireNonNull(quantity, "quantity must not be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money total() {
        return unitPrice.multiply(quantity.getValue());
    }

    public OrderItem increaseQuantity(Quantity additional) {
        return new OrderItem(productId, this.quantity.add(additional), unitPrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderItem orderItem = (OrderItem) o;
        return productId.equals(orderItem.productId) && quantity.equals(orderItem.quantity)
                && unitPrice.equals(orderItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }
}
