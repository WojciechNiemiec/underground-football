package football.underground.eventsourcing;

import football.underground.eventsourcing.spi.Event;
import football.underground.eventsourcing.spi.EventRepository;

import java.util.List;

public class EventStreamLoader<T, ID> {

    private final EventRepository<ID> loader;

    public static <T, ID> EventStreamLoader<T, ID> inMemory() {
        return new EventStreamLoader<>(new InMemoryEventRepository<>());
    }

    EventStreamLoader(EventRepository<ID> loader) {
        this.loader = loader;
    }

    public EventStream<ID, T> load(ID aggregateId, EventStream.Configuration<T> configuration) {
        List<Event<ID>> events = loader.load(aggregateId);
        return new EventStream<>(aggregateId, configuration, events);
    }

    public void save(EventStream<ID, T> eventStream) {
        loader.save(eventStream.newEvents());
    }
}
