package com.pedidos.application.port.out;

import java.time.Instant;

/**
 * Port for obtaining current time (allows testing/time control).
 */
public interface Clock {
    Instant now();
}
