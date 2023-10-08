package football.underground.eventsourcing.spi;

import java.time.Instant;

public record Event<ID>(
        ID aggregateId,
        Instant timestamp,
        Class<?> payloadType,
        Object payload) {
}
