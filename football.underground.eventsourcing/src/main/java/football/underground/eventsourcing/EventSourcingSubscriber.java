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

    public <V> void subscribe(Class<V> eventType, BiConsumer<T, V> sourcingHandler) {
        Handler<T, ID, V> handler = (aggregate, event, id, date) -> sourcingHandler.accept(
                aggregate,
                event
        );
        handlers.put(eventType, new HandlerExecutor<>(eventType, handler));
    }

    public <V> void subscribeWithMeta(Class<V> eventType, Handler<T, ID, V> sourcingHandler) {
        handlers.put(eventType, new HandlerExecutor<>(eventType, sourcingHandler));
    }

    Optional<EventSourcingSubscriber<T, ID>.HandlerExecutor<?>> getHandlerExecutor(Event<ID> event) {
        var handler = handlers.get(event.payload().getClass());
        return Optional.ofNullable(handler);
    }

    public interface Handler<T, ID, V> {
        void handle(T consumer, V event, ID aggregateId, Instant date);
    }

    class HandlerExecutor<V> {
        private final Class<V> eventType;
        private final Handler<T, ID, V> handler;

        HandlerExecutor(Class<V> eventType, Handler<T, ID, V> handler) {
            this.eventType = eventType;
            this.handler = handler;
        }

        void execute(Event<ID> event) {
            if (eventType.equals(event.payload().getClass())) {
                handler.handle(entity, eventType.cast(event.payload()), event.aggregateId(), event.timestamp());
            }
        }
    }
}
