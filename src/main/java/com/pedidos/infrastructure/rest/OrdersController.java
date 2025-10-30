package com.pedidos.infrastructure.rest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos.application.dto.AddItemToOrderRequest;
import com.pedidos.application.dto.CreateOrderRequest;
import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.ConflictError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.errors.NotFoundError;
import com.pedidos.application.errors.ValidationError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.application.port.out.OrderRepository;
import com.pedidos.application.usecase.AddItemToOrderUseCase;
import com.pedidos.application.usecase.CreateOrderUseCase;
import com.pedidos.application.usecase.DeleteOrderUseCase;
import com.pedidos.application.usecase.GetOrderUseCase;
import com.pedidos.shared.result.Result;

/**
 * REST controller que expone endpoints para pedidos.
 */
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderRepository repository;
    private final EventBus eventBus;

    public OrdersController(OrderRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    // HTTP request models to decouple Jackson mapping from application DTOs
    public static final class ItemDto {
        public String productId;
        public int quantity;
        public BigDecimal unitPrice;
        public String currency;

        public ItemDto() {
        }
    }

    public static final class CreateOrderHttp {
        public List<ItemDto> items;

        public CreateOrderHttp() {
        }
    }

    public static final class AddItemHttp {
        public String productId;
        public int quantity;
        public BigDecimal unitPrice;
        public String currency;

        public AddItemHttp() {
        }
    }

    // Responses
    public static final class CreatedResponse {
        public String orderId;

        public CreatedResponse(String orderId) {
            this.orderId = orderId;
        }
    }

    public static final class ErrorResponse {
        public String error;
        public String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
    }

    // Order GET response
    public static final class OrderItemResp {
        public String productId;
        public int quantity;
        public java.math.BigDecimal unitPrice;
        public String currency;

        public OrderItemResp(String productId, int quantity, java.math.BigDecimal unitPrice, String currency) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.currency = currency;
        }
    }

    public static final class OrderResponse {
        public String orderId;
        public List<OrderItemResp> items;
        public Map<String, java.math.BigDecimal> totals;

        public OrderResponse(String orderId, List<OrderItemResp> items, Map<String, java.math.BigDecimal> totals) {
            this.orderId = orderId;
            this.items = items;
            this.totals = totals;
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderHttp body) {
        if (body == null || body.items == null || body.items.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("validation_error", "Order must contain at least one item"));
        }

        // map to application DTO
        List<CreateOrderRequest.Item> items = body.items.stream()
                .map(i -> new CreateOrderRequest.Item(i.productId, i.quantity, i.unitPrice, i.currency))
                .toList();

        CreateOrderRequest req = new CreateOrderRequest(items);

        CreateOrderUseCase uc = new CreateOrderUseCase(repository, eventBus);
        Result<?, AppError> res = uc.execute(req);
        if (res.isOk()) {
            Object value = res.getValue();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreatedResponse(Objects.toString(value, null)));
        } else {
            return mapError(res.getError());
        }
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItem(@PathVariable String orderId, @RequestBody AddItemHttp body) {
        if (body == null)
            return ResponseEntity.badRequest().body(new ErrorResponse("validation_error", "Invalid body"));

        AddItemToOrderRequest req = new AddItemToOrderRequest(orderId, body.productId, body.quantity, body.unitPrice,
                body.currency);
        AddItemToOrderUseCase uc = new AddItemToOrderUseCase(repository, eventBus);
        Result<?, AppError> res = uc.execute(req);
        if (res.isOk()) {
            Object value = res.getValue();
            return ResponseEntity.ok(new CreatedResponse(Objects.toString(value, null)));
        } else {
            return mapError(res.getError());
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        GetOrderUseCase getUc = new GetOrderUseCase(repository);
        Result<Optional<com.pedidos.domain.entities.Order>, AppError> res = getUc.execute(orderId);
        if (!res.isOk()) {
            return mapError(res.getError());
        }

        Optional<com.pedidos.domain.entities.Order> maybe = res.getValue();
        if (maybe.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("not_found", "Order not found"));
        }

        com.pedidos.domain.entities.Order order = maybe.get();

        List<OrderItemResp> items = order.getItems().stream().map(it -> new OrderItemResp(
                it.getProductId().getId(), it.getQuantity().getValue(), it.getUnitPrice().getAmount(),
                it.getUnitPrice().getCurrency().getCode())).toList();

        Map<String, java.math.BigDecimal> totals = order.totalsByCurrency().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(e -> e.getKey().getCode(), e -> e.getValue().getAmount(),
                        (a, b) -> a, java.util.LinkedHashMap::new));

        return ResponseEntity.ok(new OrderResponse(order.getId().toString(), items, totals));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
        DeleteOrderUseCase delUc = new DeleteOrderUseCase(repository, eventBus);
        Result<Void, AppError> res = delUc.execute(orderId);
        if (!res.isOk()) {
            return mapError(res.getError());
        }
        // Successful delete — return 200 OK
        return ResponseEntity.ok("Deleted successfully: " + orderId);
    }

    private ResponseEntity<Object> mapError(AppError err) {
        Objects.requireNonNull(err);
        if (err instanceof ValidationError ve) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("validation_error", ve.message()));
        }
        if (err instanceof NotFoundError nf) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("not_found", nf.message()));
        }
        if (err instanceof ConflictError cf) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("conflict", cf.message()));
        }
        if (err instanceof InfraError ie) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("infra_error", ie.message()));
        }
        // generic fallback
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("error", Objects.toString(err, "Unknown error")));
    }
}
