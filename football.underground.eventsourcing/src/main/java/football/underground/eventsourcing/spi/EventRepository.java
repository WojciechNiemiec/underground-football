package football.underground.eventsourcing.spi;

import java.util.List;

public interface EventRepository<ID> {
    void save(List<Event<ID>> events);

    List<Event<ID>> load(ID aggregateId);
}
