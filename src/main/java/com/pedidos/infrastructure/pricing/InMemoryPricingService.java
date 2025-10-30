package com.pedidos.infrastructure.pricing;

import java.math.BigDecimal;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.port.out.PricingService;
import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.shared.result.Result;

/**
 * Simple in-memory pricing service useful for development and tests.
 */
public final class InMemoryPricingService implements PricingService {
    @Override
    public Result<Money, AppError> priceFor(ProductId productId) {
        // Demo: return a fixed price based on productId hash for determinism
        int hash = Math.abs(productId.getId().hashCode());
        BigDecimal price = BigDecimal.valueOf(5 + (hash % 20));
        Money m = new Money(price, Currency.EUR());
        return Result.ok(m);
    }
}
