package com.pedidos.infrastructure.configuration;

import java.util.Objects;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pedidos.application.port.out.Clock;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.application.port.out.PricingService;
import com.pedidos.infrastructure.clock.SystemClock;
import com.pedidos.infrastructure.eventbus.InMemoryEventBus;
import com.pedidos.infrastructure.persistence.h2.H2OrderRepository;
import com.pedidos.infrastructure.pricing.InMemoryPricingService;
import com.pedidos.shared.result.Result;

/**
 * Spring configuration that wires basic infrastructure beans for development.
 */
@Configuration
public class InfrastructureConfiguration {

    @Bean
    public DataSource dataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:pedidos;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    public OrderRepository orderRepository(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        return new H2OrderRepository(dataSource);
    }

    @Bean
    public EventBus eventBus() {
        InMemoryEventBus bus = new InMemoryEventBus();
        // register a simple logger handler for debugging
        bus.register(Object.class, event -> {
            System.out.println("[event] " + event);
            return Result.ok(null);
        });
        return bus;
    }

    @Bean
    public PricingService pricingService() {
        return new InMemoryPricingService();
    }

    @Bean
    public Clock clock() {
        return new SystemClock();
    }
}
