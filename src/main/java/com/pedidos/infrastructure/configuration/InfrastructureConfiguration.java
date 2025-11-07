package com.pedidos.infrastructure.configuration;

import java.util.Objects;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.pedidos.application.port.out.Clock;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.infrastructure.adapter.persistence.h2.H2OrderRepository;
import com.pedidos.infrastructure.adapter.persistence.jpa.JpaOrderRepository;
import com.pedidos.infrastructure.adapter.persistence.jpa.SpringDataOrderRepositoryAdapter;
import com.pedidos.infrastructure.clock.SystemClock;
import com.pedidos.infrastructure.eventbus.InMemoryEventBus;
import com.pedidos.shared.result.Result;

/**
 * Spring configuration that wires basic infrastructure beans for development
 * and production.
 */
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class) // Inject DatabaseProperties
public class InfrastructureConfiguration {

    private final DatabaseProperties dbProps;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InfrastructureConfiguration.class);

    public InfrastructureConfiguration(DatabaseProperties dbProps) {
        this.dbProps = dbProps;
    }

    @Bean
    public DataSource dataSource() {
        log.info("Configuring DataSource for DB_KIND={}", dbProps.getKind());
        if ("POSTGRES".equalsIgnoreCase(dbProps.getKind())) {
            return DataSourceBuilder.create()
                    .url(dbProps.getUrl())
                    .username(dbProps.getUser())
                    .password(dbProps.getPass())
                    .build();
        } else {
            log.info("Using in-memory H2 database");
            return DataSourceBuilder.create()
                    .url("jdbc:h2:mem:pedidos;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                    .username("sa")
                    .build();
        }
    }

    @Bean
    @Profile("!prod")
    public OrderRepository orderRepositoryDev(DataSource dataSource, java.util.Optional<JpaOrderRepository> jpaRepo) {
        Objects.requireNonNull(dataSource);
        // If JPA repository bean exists (JPA on classpath and entities enabled),
        // use the Spring Data adapter automatically.
        if (jpaRepo != null && jpaRepo.isPresent()) {
            return new SpringDataOrderRepositoryAdapter(jpaRepo.get());
        }

        // If DB_KIND requests Postgres but JPA is not available, fail early.
        if (dataSource instanceof PGSimpleDataSource) {
            throw new IllegalStateException(
                    "Detected Postgres DataSource in dev but JPA is not available. Add 'spring-boot-starter-data-jpa' or set DB_KIND=H2 in .env");
        }

        return new H2OrderRepository(dataSource);
    }

    @Bean
    @Profile("prod")
    public OrderRepository orderRepositoryProd(DataSource dataSource, java.util.Optional<JpaOrderRepository> jpaRepo,
            Environment env) {
        Objects.requireNonNull(dataSource);
        // Prefer JPA adapter in production if available
        if (jpaRepo != null && jpaRepo.isPresent()) {
            return new SpringDataOrderRepositoryAdapter(jpaRepo.get());
        }

        throw new IllegalStateException(
                "Production requires JPA adapter (spring-boot-starter-data-jpa). Add the dependency and configure spring.datasource.*");
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
}
