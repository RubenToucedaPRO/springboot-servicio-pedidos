package com.pedidos.infrastructure.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pedidos.application.errors.AppError;
import com.pedidos.application.errors.InfraError;
import com.pedidos.application.port.out.EventBus;
import com.pedidos.shared.result.Result;

/**
 * EventBus síncrono en memoria.
 *
 * - Registro por tipo de evento: register(EventClass, handler)
 * - Publicación síncrona: publish(event) invoca handlers en el hilo llamador.
 * - Si algún handler devuelve Result.fail(...) se devuelve ese fallo (el
 * primero).
 * - Excepciones lanzadas por handlers se mapean a InfraError.
 */
public final class InMemoryEventBus implements EventBus {

    private final Map<Class<?>, List<EventHandler<?>>> handlers = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);

    /**
     * Registra un handler para un tipo de evento concreto.
     */
    public <E> void register(Class<E> eventType, EventHandler<E> handler) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(handler);
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * Desregistra un handler (si estaba registrado).
     */
    public <E> void unregister(Class<E> eventType, EventHandler<E> handler) {
        List<EventHandler<?>> list = handlers.get(eventType);
        if (list != null) {
            list.remove(handler);
            if (list.isEmpty())
                handlers.remove(eventType);
        }
    }

    @Override
    public Result<Void, AppError> publish(Object event) {
        if (event == null)
            return Result.ok(null);

        Class<?> eventClass = event.getClass();

        log.debug("Publishing event of type {} to handlers", eventClass.getSimpleName());

        // Recolectar handlers aplicables (incluye superclases/interfaces)
        List<EventHandler<?>> applicable = new ArrayList<>();
        for (Map.Entry<Class<?>, List<EventHandler<?>>> e : handlers.entrySet()) {
            if (e.getKey().isAssignableFrom(eventClass)) {
                applicable.addAll(e.getValue());
            }
        }

        // Ejecutar handlers uno a uno de forma síncrona
        for (EventHandler<?> h : applicable) {
            try {
                @SuppressWarnings("unchecked")
                EventHandler<Object> eh = (EventHandler<Object>) h;
                Result<Void, AppError> res = eh.handle(event);
                if (res == null) {
                    log.error("Event handler returned null Result for event {}", eventClass.getSimpleName());
                    return Result.fail(new InfraError("Event handler returned null Result",
                            new NullPointerException("handler returned null")));
                }
                if (res.isFail()) {
                    log.warn("Event handler returned failure for event {}: {}", eventClass.getSimpleName(),
                            res.getError());
                    return Result.fail(res.getError());
                }
            } catch (Exception ex) {
                log.error("Event handler threw exception for event {}: {}", eventClass.getSimpleName(), ex.toString());
                return Result.fail(new InfraError("Event handler threw exception: " + ex.getMessage(), ex));
            }
        }
        log.debug("Event {} published successfully", eventClass.getSimpleName());
        return Result.ok(null);
    }
}