package football.underground.eventsourcing;

import football.underground.eventsourcing.exception.IncorrectHandlerException;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EventStream<T> implements Appender {
    private final UUID aggregateId;
    private final List<Event> events;
    private final Clock clock;
    private final Map<Class<?>, HandlerExecutor<T, ?>> handlers;

    private T aggregate;

    public EventStream(UUID aggregateId, Configuration<T> configuration) {
        this.aggregateId = aggregateId;
        this.events = new ArrayList<>();
        var subscriber = new Subscriber<T>();
        configuration.registerHandlers(subscriber);
        this.handlers = new HashMap<>(subscriber.handlers);
        this.clock = configuration.getClock();
        this.events.forEach(event -> getHandlerExecutor(event).execute(aggregate, event));
    }

    @Override
    public <V> void append(V eventPayload) {
        var event = new Event(aggregateId, clock.instant(), eventPayload.getClass(), eventPayload);
        events.add(event);
        getHandlerExecutor(event).execute(aggregate, event);
    }

    private HandlerExecutor<T, ?> getHandlerExecutor(Event event) {
        HandlerExecutor<T, ?> handler = handlers.get(event.payloadType());
        if (handler == null) {
            throw new IncorrectHandlerException();
        }
        return handler;
    }

    public T load(Function<EventStream<T>, T> constructor) {
        aggregate = constructor.apply(this);
        events.forEach(event -> getHandlerExecutor(event).execute(aggregate, event));
        return aggregate;
    }

    public static class Subscriber<T> {
        private final Map<Class<?>, HandlerExecutor<T, ?>> handlers = new HashMap<>();

        public <V> void subscribe(Class<V> eventType, BiConsumer<T, V> sourcingHandler) {
            Handler<T, V> handler = (aggregate, event, date) -> sourcingHandler.accept(aggregate, event);
            handlers.put(eventType, new HandlerExecutor<>(eventType, handler));
        }

        public <V> void subscribeWithMeta(Class<V> eventType, Handler<T, V> sourcingHandler) {
            handlers.put(eventType, new HandlerExecutor<>(eventType, sourcingHandler));
        }
    }

    public interface Handler<T, V> {
        void handle(T aggregate, V event, Instant date);
    }

    public interface Configuration<T> {
        void registerHandlers(Subscriber<T> subscriber);

        default Clock getClock() {
            return Clock.systemDefaultZone();
        }
    }

    private record HandlerExecutor<T, V>(Class<V> eventType, Handler<T, V> handler) {
        @SuppressWarnings("unchecked")
        void execute(T aggregate, Event event) {
            if (eventType.equals(event.payloadType())) {
                handler.handle(aggregate, (V) event.payload(), event.timestamp());
            }
        }
    }

    record Event(
            UUID aggregateId,
            Instant timestamp,
            Class<?> payloadType,
            Object payload) {
    }
}
