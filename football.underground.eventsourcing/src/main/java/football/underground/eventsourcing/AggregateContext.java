package football.underground.eventsourcing;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Function;

public class AggregateContext<T extends V, V, ID> {

    private final EventRepository<ID> repository;

    public AggregateContext(EventRepository<ID> repository) {
        this.repository = repository;
    }

    public V load(
            ID aggregateId,
            EventSourcingConfiguration<T, ID> configuration,
            Function<Appender, T> constructor,
            Class<V> interfaceType
    ) {
        List<Event<ID>> events = repository.load(aggregateId);
        var stream = new EventStream<>(aggregateId, configuration, events, constructor);
        T aggregate = stream.load();
        return interfaceType.cast(Proxy.newProxyInstance(
                aggregate.getClass().getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    Object result = method.invoke(aggregate, args);
                    repository.save(stream.newEvents());
                    return result;
                }
        ));
    }
}
