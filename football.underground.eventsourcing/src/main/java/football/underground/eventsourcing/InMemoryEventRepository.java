package football.underground.eventsourcing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class InMemoryEventRepository<ID> implements EventRepository<ID> {

    private final List<Event<ID>> events = new ArrayList<>();
    private final List<EventSourcingSubscriber<?, ID>> subscribers = new ArrayList<>();

    @Override
    public void save(List<Event<ID>> events) {
        this.events.addAll(events);
        events.forEach(event -> subscribers.forEach(subscriber -> subscriber.getHandlerExecutor(event)
                .ifPresent(executor -> executor.execute(event))));
    }

    @Override
    public List<Event<ID>> load(ID aggregateId) {
        return events.stream()
                .filter(event -> event.aggregateId().equals(aggregateId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <T> void subscribe(T entity, EventSourcingConfiguration<T, ID> configuration) {
        var subscriber = new EventSourcingSubscriber<T, ID>(entity);
        configuration.registerHandlers(subscriber);
        subscribers.add(subscriber);
    }
}
