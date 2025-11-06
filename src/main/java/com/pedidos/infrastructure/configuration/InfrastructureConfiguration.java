package com.pedidos.infrastructure.configuration;

import java.util.Objects;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.pedidos.application.port.out.Clock;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.infrastructure.clock.SystemClock;
import com.pedidos.infrastructure.eventbus.InMemoryEventBus;
import com.pedidos.infrastructure.persistence.h2.H2OrderRepository;
import com.pedidos.infrastructure.persistence.postgres.PostgresOrderRepository;
import com.pedidos.shared.result.Result;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Spring configuration that wires basic infrastructure beans for development
 * and production.
 */
@Configuration
public class InfrastructureConfiguration {

    @Bean
    @Profile("!prod")
    public DataSource dataSourceDev() {
        // Load .env if present (development convenience) and fall back to system env
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String dbKind = getenvOrDotenv("DB_KIND", dotenv, "H2");
        // Support POSTGRES or H2 (default)
        if ("POSTGRES".equalsIgnoreCase(dbKind)) {
            String url = getenvOrDotenv("DB_URL", dotenv, "jdbc:postgresql://localhost:5432/pedidos");
            String user = getenvOrDotenv("DB_USER", dotenv, "postgres");
            String pass = getenvOrDotenv("DB_PASS", dotenv, "");

            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setUrl(url);
            if (user != null && !user.isEmpty())
                ds.setUser(user);
            if (pass != null)
                ds.setPassword(pass);
            return ds;
        }
        // Default to H2 in-memory database
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:pedidos;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    @Profile("prod")
    public DataSource dataSourceProd(Environment env) {
        // In production prefer Spring properties (application-prod.yml or env vars)
        String url = env.getProperty("spring.datasource.url");
        String user = env.getProperty("spring.datasource.username");
        String pass = env.getProperty("spring.datasource.password");
        String driver = env.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

        if (url == null || url.isBlank()) {
            throw new IllegalStateException("spring.datasource.url must be set for prod profile");
        }

        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        builder.url(url).username(user).password(pass).driverClassName(driver);
        return builder.build();
    }

    @Bean
    @Profile("!prod")
    public OrderRepository orderRepositoryDev(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        // If the dev DataSource is actually a Postgres DS (when DB_KIND=POSTGRES)
        // prefer the Postgres adapter
        if (dataSource instanceof PGSimpleDataSource) {
            return new PostgresOrderRepository(dataSource);
        }
        return new H2OrderRepository(dataSource);
    }

    @Bean
    @Profile("prod")
    public OrderRepository orderRepositoryProd(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        return new PostgresOrderRepository(dataSource);
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
    public Clock clock() {
        return new SystemClock();
    }

    private static String getenvOrDotenv(String key, Dotenv dotenv, String defaultValue) {
        String v = System.getenv(key);
        if (v != null && !v.isBlank())
            return v;
        try {
            String dv = dotenv.get(key);
            if (dv != null && !dv.isBlank())
                return dv;
        } catch (Exception e) {
            // ignore
        }
        return defaultValue;
    }
}
