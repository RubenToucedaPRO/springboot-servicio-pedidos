package com.pedidos.infrastructure.persistence.postgres;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.sql.DataSource;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.domain.entities.Order;
import com.pedidos.domain.valueobjects.Currency;
import com.pedidos.domain.valueobjects.Money;
import com.pedidos.domain.valueobjects.OrderId;
import com.pedidos.domain.valueobjects.OrderItem;
import com.pedidos.domain.valueobjects.ProductId;
import com.pedidos.domain.valueobjects.Quantity;
import com.pedidos.shared.result.Result;

/**
 * JDBC implementation for Postgres. Similar behavior to H2OrderRepository but
 * uses Postgres upsert semantics.
 */
public class PostgresOrderRepository implements OrderRepository {
    private final DataSource dataSource;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PostgresOrderRepository.class);

    public PostgresOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        try (Connection c = dataSource.getConnection()) {
            ensureSchema(c);
            log.info("PostgresOrderRepository initialized and schema ensured");
        } catch (SQLException e) {
            log.error("Failed to initialize PostgresOrderRepository schema: {}", e.toString());
            throw new RuntimeException("Failed to initialize PostgresOrderRepository schema", e);
        }
    }

    private void ensureSchema(Connection c) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "CREATE TABLE IF NOT EXISTS orders (id VARCHAR(36) PRIMARY KEY, created_at TIMESTAMP)")) {
            p.execute();
        }
        try (PreparedStatement p = c.prepareStatement(
                "CREATE TABLE IF NOT EXISTS order_items (order_id VARCHAR(36), product_id VARCHAR(255), quantity INT, unit_amount NUMERIC(19,2), currency VARCHAR(8), PRIMARY KEY(order_id, product_id), FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE)")) {
            p.execute();
        }
    }

    @Override
    public Result<Void, AppError> save(Order order) {
        log.debug("PostgresOrderRepository.save - orderId={} items={}", order.getId(), order.getItems().size());
        String sqlInsertOrder = "INSERT INTO orders (id, created_at) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at";
        String sqlDeleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlInsertItem = "INSERT INTO order_items(order_id, product_id, quantity, unit_amount, currency) VALUES (?, ?, ?, ?, ?)";

        try (Connection c = dataSource.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement pOrder = c.prepareStatement(sqlInsertOrder)) {
                pOrder.setString(1, order.getId().getId().toString());
                pOrder.setTimestamp(2, Timestamp.from(Instant.now()));
                pOrder.executeUpdate();
            }

            try (PreparedStatement pDel = c.prepareStatement(sqlDeleteItems)) {
                pDel.setString(1, order.getId().getId().toString());
                pDel.executeUpdate();
            }

            try (PreparedStatement pItem = c.prepareStatement(sqlInsertItem)) {
                for (OrderItem it : order.getItems()) {
                    pItem.setString(1, order.getId().getId().toString());
                    pItem.setString(2, it.getProductId().getId());
                    pItem.setInt(3, it.getQuantity().getValue());
                    Money total = it.getUnitPrice();
                    pItem.setBigDecimal(4, total.getAmount());
                    pItem.setString(5, total.getCurrency().getCode());
                    pItem.addBatch();
                }
                pItem.executeBatch();
            }

            c.commit();
            c.setAutoCommit(oldAuto);
            log.info("PostgresOrderRepository.save - saved order {}", order.getId());
            return Result.ok(null);
        } catch (SQLException e) {
            log.error("PostgresOrderRepository.save - failed to save order {}: {}", order.getId(), e.toString());
            return Result.fail(new InfraError("Failed to save order: " + e.getMessage(), e));
        }
    }

    @Override
    public Result<Optional<Order>, AppError> findById(OrderId id) {
        String sqlOrder = "SELECT id, created_at FROM orders WHERE id = ?";
        String sqlItems = "SELECT product_id, quantity, unit_amount, currency FROM order_items WHERE order_id = ? ORDER BY product_id";
        // Consultar order y si existe
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sqlOrder)) {
                p.setString(1, id.getId().toString());
                try (ResultSet rs = p.executeQuery()) {
                    if (!rs.next()) {
                        return Result.ok(Optional.empty());
                    }
                }
            }

            Order order = Order.create(id);
            // Si existe, cargar items
            try (PreparedStatement pItems = c.prepareStatement(sqlItems)) {
                pItems.setString(1, id.getId().toString());
                try (ResultSet rs = pItems.executeQuery()) {
                    while (rs.next()) {
                        String productId = rs.getString("product_id");
                        int quantity = rs.getInt("quantity");
                        BigDecimal unitAmount = rs.getBigDecimal("unit_amount");
                        String currencyCode = rs.getString("currency");

                        ProductId pid = new ProductId(productId);
                        Currency currency = Currency.of(currencyCode);
                        Money unitPrice = new Money(unitAmount, currency);
                        Quantity qty = new Quantity(quantity);
                        order.addItem(new OrderItem(pid, qty, unitPrice));
                    }
                }
            }

            return Result.ok(Optional.of(order));
        } catch (SQLException e) {
            log.error("PostgresOrderRepository.findById - SQL error for id {}: {}", id, e.toString());
            return Result.fail(new InfraError("Failed to query order: " + e.getMessage(), e));
        } catch (Exception e) {
            log.error("PostgresOrderRepository.findById - mapping error for id {}: {}", id, e.toString());
            return Result.fail(new InfraError("Failed to map order from DB: " + e.getMessage(), e));
        }
    }

    @Override
    public Result<Void, AppError> delete(OrderId id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, id.getId().toString());
                p.executeUpdate();
            }
            log.info("PostgresOrderRepository.delete - deleted order {}", id);
            return Result.ok(null);
        } catch (SQLException e) {
            log.error("PostgresOrderRepository.delete - failed to delete {}: {}", id, e.toString());
            return Result.fail(new InfraError("Failed to delete order: " + e.getMessage(), e));
        }
    }
}
