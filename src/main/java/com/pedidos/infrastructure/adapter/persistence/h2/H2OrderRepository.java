package com.pedidos.infrastructure.adapter.persistence.h2;

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
import com.pedidos.application.errors.NotFoundError;
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
 * Simple JDBC implementation of OrderRepository for H2.
 *
 * - Expects a DataSource connected to an H2 database.
 * - Creates tables if they don't exist.
 * - Uses simple transactional save semantics: replace items for an order.
 */
public class H2OrderRepository implements OrderRepository {
    private final DataSource dataSource;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(H2OrderRepository.class);

    public H2OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        try (Connection c = dataSource.getConnection()) {
            ensureSchema(c);
            log.info("H2OrderRepository initialized and schema ensured");
        } catch (SQLException e) {
            log.error("Failed to initialize H2OrderRepository schema: {}", e.toString());
            throw new RuntimeException("Failed to initialize H2OrderRepository schema", e);
        }
    }

    private void ensureSchema(Connection c) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "CREATE TABLE IF NOT EXISTS orders (id VARCHAR(36) PRIMARY KEY, created_at TIMESTAMP)")) {
            p.execute();
        }
        try (PreparedStatement p = c.prepareStatement(
                "CREATE TABLE IF NOT EXISTS order_items (order_id VARCHAR(36), product_id VARCHAR(255), quantity INT, unit_amount DECIMAL(19,2), currency VARCHAR(8), PRIMARY KEY(order_id, product_id), FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE)")) {
            p.execute();
        }
    }

    @Override
    public Result<Void, AppError> save(Order order) {
        log.debug("H2OrderRepository.save - orderId={} items={}", order.getId(), order.getItems().size());
        String sqlInsertOrder = "MERGE INTO orders (id, created_at) KEY(id) VALUES (?, ?)";
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
                    // store unit price amount
                    pItem.setBigDecimal(4, total.getAmount());
                    pItem.setString(5, total.getCurrency().getCode());
                    pItem.addBatch();
                }
                pItem.executeBatch();
            }

            c.commit();
            c.setAutoCommit(oldAuto);
            log.info("H2OrderRepository.save - saved order {}", order.getId());
            return Result.ok(null);
        } catch (SQLException e) {
            log.error("H2OrderRepository.save - failed to save order {}: {}", order.getId(), e.toString());
            return Result.fail(new InfraError("Failed to save order: " + e.getMessage(), e));
        }
    }

    @Override
    public Result<Void, AppError> update(Order order) {
        log.debug("H2OrderRepository.update - orderId={} items={}", order.getId(), order.getItems().size());
        String sqlExists = "SELECT 1 FROM orders WHERE id = ?";
        String sqlMergeOrder = "MERGE INTO orders (id, created_at) KEY(id) VALUES (?, ?)";
        String sqlMergeItem = "MERGE INTO order_items (order_id, product_id, quantity, unit_amount, currency) KEY(order_id, product_id) VALUES (?,?,?,?,?)";
        String sqlDeleteExtraPrefix = "DELETE FROM order_items WHERE order_id = ? AND product_id NOT IN (";

        try (Connection c = dataSource.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // check exists
                try (PreparedStatement p = c.prepareStatement(sqlExists)) {
                    p.setString(1, order.getId().getId().toString());
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return Result.fail(new NotFoundError("Order not found: " + order.getId()));
                        }
                    }
                }

                // upsert order header
                try (PreparedStatement pOrder = c.prepareStatement(sqlMergeOrder)) {
                    pOrder.setString(1, order.getId().getId().toString());
                    pOrder.setTimestamp(2, Timestamp.from(Instant.now()));
                    pOrder.executeUpdate();
                }

                // upsert items
                try (PreparedStatement pm = c.prepareStatement(sqlMergeItem)) {
                    for (OrderItem it : order.getItems()) {
                        pm.setString(1, order.getId().getId().toString());
                        pm.setString(2, it.getProductId().getId());
                        pm.setInt(3, it.getQuantity().getValue());
                        pm.setBigDecimal(4, it.getUnitPrice().getAmount());
                        pm.setString(5, it.getUnitPrice().getCurrency().getCode());
                        pm.addBatch();
                    }
                    pm.executeBatch();
                }

                // delete items removed from aggregate
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    String placeholders = String.join(
                            ",",
                            java.util.Collections.nCopies(order.getItems().size(), "?"));
                    String sqlDeleteExtra = sqlDeleteExtraPrefix + placeholders + ")";
                    try (PreparedStatement pd = c.prepareStatement(sqlDeleteExtra)) {
                        pd.setString(1, order.getId().getId().toString());
                        int idx = 2;
                        for (OrderItem it : order.getItems()) {
                            pd.setString(idx++, it.getProductId().getId());
                        }
                        pd.executeUpdate();
                    }
                } else {
                    // if no items, remove all items
                    try (PreparedStatement pDel = c.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                        pDel.setString(1, order.getId().getId().toString());
                        pDel.executeUpdate();
                    }
                }

                c.commit();
                c.setAutoCommit(oldAuto);
                log.info("H2OrderRepository.update - updated order {}", order.getId());
                return Result.ok(null);
            } catch (SQLException e) {
                try {
                    c.rollback();
                } catch (SQLException ignore) {
                }
                log.error("H2OrderRepository.update - failed to update order {}: {}", order.getId(), e.toString());
                return Result.fail(new InfraError("Failed to update order: " + e.getMessage(), e));
            } finally {
                try {
                    c.setAutoCommit(oldAuto);
                } catch (SQLException ignore) {
                }
            }
        } catch (SQLException e) {
            log.error("H2OrderRepository.update - connection error for {}: {}", order.getId(), e.toString());
            return Result.fail(new InfraError("Failed to open DB connection: " + e.getMessage(), e));
        }
    }

    @Override
    public Result<Optional<Order>, AppError> findById(OrderId id) {
        String sqlOrder = "SELECT id, created_at FROM orders WHERE id = ?";
        String sqlItems = "SELECT product_id, quantity, unit_amount, currency FROM order_items WHERE order_id = ? ORDER BY product_id";

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
            log.error("H2OrderRepository.findById - SQL error for id {}: {}", id, e.toString());
            return Result.fail(new InfraError("Failed to query order: " + e.getMessage(), e));
        } catch (Exception e) {
            // any mapping/domain exception
            log.error("H2OrderRepository.findById - mapping error for id {}: {}", id, e.toString());
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
                // idempotent delete: if no rows affected, order did not exist, but return ok
            }
            log.info("H2OrderRepository.delete - deleted order {}", id);
            return Result.ok(null);
        } catch (SQLException e) {
            log.error("H2OrderRepository.delete - failed to delete {}: {}", id, e.toString());
            return Result.fail(new InfraError("Failed to delete order: " + e.getMessage(), e));
        }
    }
}
