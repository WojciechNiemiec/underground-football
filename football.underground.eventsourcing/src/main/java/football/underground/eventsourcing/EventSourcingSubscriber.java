package football.underground.eventsourcing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EventSourcingSubscriber<T, ID> {
    private final T entity;
    private final Map<Class<?>, HandlerExecutor<?>> handlers = new HashMap<>();

    public EventSourcingSubscriber(T entity) {
        this.entity = entity;
    }

    /**
     * Register event handler for specific event type.
     * Multiple handlers can be registered for the same event type.
     *
     * @param eventType - class representing event payload type
     * @param sourcingHandler - idempotent event handling callback (at least one delivery guaranteed)
     * @param <V> - payload type
     */
    public <V> void subscribe(Class<V> eventType, BiConsumer<T, V> sourcingHandler) {
        Handler<T, ID, V> handler = (aggregate, event, id, date) -> sourcingHandler.accept(
                aggregate,
                event
        );
        handlers.put(eventType, new HandlerExecutor<>(eventType, handler));
    }

    /**
     * Register event handler for specific event type.
     * Multiple handlers can be registered for the same event type.
     *
     * @param eventType - class representing event payload type
     * @param sourcingHandler - idempotent event handling callback (at least one delivery guaranteed)
     * @param <V> - payload type
     */
    public <V> void subscribeWithMeta(Class<V> eventType, Handler<T, ID, V> sourcingHandler) {
        handlers.put(eventType, new HandlerExecutor<>(eventType, sourcingHandler));
    }

    public Optional<EventSourcingSubscriber<T, ID>.HandlerExecutor<?>> getHandlerExecutor(Event<ID> event) {
        var handler = handlers.get(event.payload().getClass());
        return Optional.ofNullable(handler);
    }

    public interface Handler<T, ID, V> {
        void handle(T consumer, V event, ID aggregateId, Instant date);
    }

    public class HandlerExecutor<V> {
        private final Class<V> eventType;
        private final Handler<T, ID, V> handler;

        HandlerExecutor(Class<V> eventType, Handler<T, ID, V> handler) {
            this.eventType = eventType;
            this.handler = handler;
        }

        public void execute(Event<ID> event) {
            if (eventType.equals(event.payload().getClass())) {
                handler.handle(entity, eventType.cast(event.payload()), event.aggregateId(), event.timestamp());
            }
        }
    }
}
