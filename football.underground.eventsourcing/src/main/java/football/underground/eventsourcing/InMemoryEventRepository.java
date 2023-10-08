package football.underground.eventsourcing;

import football.underground.eventsourcing.spi.Event;
import football.underground.eventsourcing.spi.EventRepository;

import java.util.ArrayList;
import java.util.List;

class InMemoryEventRepository<ID> implements EventRepository<ID> {

    private final List<Event<ID>> events = new ArrayList<>();

    @Override
    public void save(List<Event<ID>> events) {
        this.events.addAll(events);
    }

    @Override
    public List<Event<ID>> load(ID aggregateId) {
        return events.stream()
                .filter(event -> event.aggregateId().equals(aggregateId))
                .toList();
    }
}
