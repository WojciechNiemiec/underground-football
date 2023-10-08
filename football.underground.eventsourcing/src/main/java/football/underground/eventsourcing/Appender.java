package football.underground.eventsourcing;

public interface Appender {
    <V> void append(V eventPayload);
}
