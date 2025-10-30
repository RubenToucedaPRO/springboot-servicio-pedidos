package com.pedidos.application.port.out;

import com.pedidos.application.errors.AppError;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.shared.result.Result;

/**
 * Port to obtain pricing information for a product.
 */
public interface PricingService {
    Result<Money, AppError> priceFor(ProductId productId);
}
