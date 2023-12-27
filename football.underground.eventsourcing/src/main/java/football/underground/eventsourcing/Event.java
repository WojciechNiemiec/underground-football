package football.underground.eventsourcing;

import java.time.Instant;

public record Event<ID>(
        String aggregateType,
        ID aggregateId,
        Instant timestamp,
        Object payload) {
}
