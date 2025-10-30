package com.pedidos.application.errors;

/**
 * Root interface for application errors used by use cases.
 *
 * This interface is intentionally open (not sealed) so external modules or
 * adapters can introduce their own AppError implementations without modifying
 * this package. Use cases should prefer pattern-matching or instanceof checks
 * and may treat unknown implementations as generic application errors.
 */
public interface AppError {

}
