package football.underground.eventsourcing;

import java.util.List;

public interface EventRepository<ID> {
    void save(List<Event<ID>> events);

    List<Event<ID>> load(ID aggregateId);

    <T> void subscribe(T entity, EventSourcingConfiguration<T, ID> configuration);

    static <ID> EventRepository<ID> inMemory() {
        return new InMemoryEventRepository<>();
    }
}
