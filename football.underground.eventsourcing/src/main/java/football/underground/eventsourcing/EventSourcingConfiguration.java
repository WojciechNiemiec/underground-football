package football.underground.eventsourcing;

import java.time.Clock;

public interface EventSourcingConfiguration<T, ID> {
    void registerHandlers(EventSourcingSubscriber<T, ID> subscriber);

    default Clock getClock() {
        return Clock.systemDefaultZone();
    }
}
