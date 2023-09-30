package football.underground.eventsourcing;

public interface EventStreamRepository<T, ID> {
    EventStream<T> load(ID aggregateId, EventStream.Configuration<T> configuration);
    void save(EventStream<T> eventStream);
}
