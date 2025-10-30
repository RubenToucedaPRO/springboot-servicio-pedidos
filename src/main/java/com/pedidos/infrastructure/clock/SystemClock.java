package com.pedidos.infrastructure.clock;

import java.time.Instant;

import com.pedidos.application.port.out.Clock;

/**
 * Simple system clock implementation.
 */
public final class SystemClock implements Clock {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
