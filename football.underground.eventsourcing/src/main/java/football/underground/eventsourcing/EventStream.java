package football.underground.eventsourcing;

import java.time.Clock;
import java.util.*;
import java.util.function.Function;

import football.underground.eventsourcing.exception.IncorrectHandlerException;

public class EventStream<T, ID> implements Appender {
    private final ID aggregateId;
    private final List<Event<ID>> events;
    private final int initialSize;
    private final Clock clock;
    private final EventSourcingSubscriber<T, ID> subscriber;

    private final T aggregate;

    public EventStream(ID aggregateId, EventSourcingConfiguration<T, ID> configuration, Function<Appender, T> constructor) {
        this(aggregateId, configuration, new ArrayList<>(), constructor);
    }

    EventStream(
            ID aggregateId,
            EventSourcingConfiguration<T, ID> configuration,
            List<Event<ID>> events,
            Function<Appender, T> constructor
    ) {
        this.aggregateId = aggregateId;
        this.events = events;
        this.initialSize = events.size();
        this.aggregate = constructor.apply(this);
        this.subscriber = new EventSourcingSubscriber<>(aggregate);
        configuration.registerHandlers(subscriber);
        this.clock = configuration.getClock();
        this.events.forEach(this::handle);
    }

    @Override
    public <V> void append(V eventPayload) {
        var event = new Event<>(aggregate.getClass().getCanonicalName(), aggregateId, clock.instant(), eventPayload);
        events.add(event);
        handle(event);
    }

    public List<Object> events() {
        return events.stream().map(Event::payload).toList();
    }

    public T load() {
        events.forEach(this::handle);
        return aggregate;
    }

    List<Event<ID>> newEvents() {
        return events.stream().skip(initialSize).toList();
    }

    private void handle(Event<ID> event) {
        subscriber.getHandlerExecutor(event).orElseThrow(IncorrectHandlerException::new).execute(event);
    }
}
