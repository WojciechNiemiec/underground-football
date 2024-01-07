package football.underground.app.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import football.underground.eventsourcing.Event;
import football.underground.eventsourcing.EventRepository;
import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.eventsourcing.EventSourcingSubscriber;

class MongoEventRepository<ID> implements EventRepository<ID> {
    private static final Logger LOG = LoggerFactory.getLogger(MongoEventRepository.class);

    private static final String EVENTS = "events";
    private static final String HEADS = "heads";
    private static final int BATCH_SIZE = 100;
    private static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(10);

    private final MongoCollection<EventDocument> eventCollection;
    private final MongoCollection<Head> headCollection;
    private final ExecutorService executorService;
    private final ObjectSerializer serializer;
    private final IDCoder<ID> idCoder;
    private final IDDecoder<ID> idDecoder;

    private final Map<String, EventSourcingSubscriber<?, ID>> subscribers = new HashMap<>();

    public MongoEventRepository(
            MongoDatabase database,
            ExecutorService executorService,
            ObjectSerializer serializer,
            IDCoder<ID> idCoder,
            IDDecoder<ID> idDecoder
    ) {
        this.eventCollection = database.getCollection(EVENTS, EventDocument.class);
        this.headCollection = database.getCollection(HEADS, Head.class);
        this.executorService = executorService;
        this.serializer = serializer;
        this.idCoder = idCoder;
        this.idDecoder = idDecoder;
    }

    @Override
    public void save(List<Event<ID>> events) {
        List<EventDocument> documents = events.stream()
                .map(event -> new EventDocument(
                        null,
                        event.aggregateType(),
                        idCoder.encode(event.aggregateId()),
                        event.timestamp(),
                        event.payload().getClass().getCanonicalName(),
                        serializer.asString(event.payload())
                ))
                .toList();

        eventCollection.insertMany(documents);

        subscribers.forEach((subscriptionId, subscriber) -> executorService.execute(() -> processEvents(
                subscriptionId,
                subscriber
        )));
    }

    @Override
    public List<Event<ID>> load(ID aggregateId) {
        return eventCollection.find(new BasicDBObject("aggregateId", idCoder.encode(aggregateId)))
                .map(this::asEvent)
                .into(new ArrayList<>());
    }

    @Override
    public <T> void subscribe(T entity, EventSourcingConfiguration<T, ID> configuration) {
        var subscriptionId = entity.getClass().getCanonicalName();
        var subscriber = new EventSourcingSubscriber<T, ID>(entity);
        configuration.registerHandlers(subscriber);
        subscribers.put(subscriptionId, subscriber);
        processEvents(subscriptionId, subscriber);
    }

    private void processEvents(String subscriptionId, EventSourcingSubscriber<?, ID> subscriber) {
        // todo: create transaction
        boolean finished;

        try {
            var isClaimed = claimHead(subscriptionId);

            if (!isClaimed) {
                return;
            }

            var head = headCollection.find(new BasicDBObject().append("subscriptionId", subscriptionId)).first();
            Objects.requireNonNull(head, "Head should be present");

            var eventDocuments = eventCollection.find(
                            new BasicDBObject()
                                    .append("timestamp", new BasicDBObject().append("$gte", head.timestamp()))
                                    .append("_id", new BasicDBObject().append("$nin", head.eventIds()))
                    )
                    .sort(new BasicDBObject("timestamp", 1))
                    .limit(BATCH_SIZE)
                    .into(new ArrayList<>());

            if (eventDocuments.isEmpty()) {
                return;
            }

            eventDocuments.forEach(document -> {
                Event<ID> event = asEvent(document);
                subscriber.getHandlerExecutor(event).ifPresent(handler -> handler.execute(event));
            });
            releaseAndMoveHead(subscriptionId, eventDocuments);

            finished = eventDocuments.size() < BATCH_SIZE;
            // todo: commit transaction
        } catch (Exception e) {
            LOG.error("Error processing events", e);
            // todo: rollback transaction
            return;
        }

        if (!finished) {
            processEvents(subscriptionId, subscriber);
        }
    }

    private boolean claimHead(String subscriptionId) {
        var updateResult = headCollection.updateOne(
                new BasicDBObject()
                        .append("subscriptionId", subscriptionId)
                        .append("$or", List.of(
                                new BasicDBObject("locked", false),
                                new BasicDBObject(
                                        "timestamp",
                                        new BasicDBObject("$lt", Instant.now().minus(PROCESSING_TIMEOUT))
                                )
                        )),
                new BasicDBObject()
                        .append("$setOnInsert", new BasicDBObject()
                                .append("subscriptionId", subscriptionId)
                                .append("timestamp", Instant.EPOCH)
                                .append("eventIds", List.of())
                        )
                        .append("$set", new BasicDBObject()
                                .append("locked", true)
                        )
                , new UpdateOptions().upsert(true)
        );

        return updateResult.wasAcknowledged() &&
               (updateResult.getUpsertedId() != null || updateResult.getMatchedCount() == 1);
    }

    private void releaseAndMoveHead(String subscriptionId, List<EventDocument> eventDocuments) {
        var headTimestamp = new LinkedList<>(eventDocuments).getLast().timestamp();
        var headEventIds = eventDocuments.stream()
                .filter(eventDocument -> eventDocument.timestamp()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .equals(headTimestamp.truncatedTo(ChronoUnit.SECONDS)))
                .map(EventDocument::eventId).toList();

        headCollection.updateOne(
                new BasicDBObject("subscriptionId", subscriptionId),
                new BasicDBObject("$set", new BasicDBObject()
                        .append("locked", false)
                        .append("timestamp", headTimestamp)
                        .append("eventIds", headEventIds)
                )
        );
    }

    private Event<ID> asEvent(EventDocument eventDocument) {
        return new Event<>(
                eventDocument.aggregateType(),
                idDecoder.decode(eventDocument.aggregateId()),
                eventDocument.timestamp(),
                serializer.fromString(eventDocument.payloadType(), eventDocument.payload())
        );
    }

    public record EventDocument(
            @BsonId
            ObjectId eventId,
            String aggregateType,
            String aggregateId,
            Instant timestamp,
            String payloadType,
            String payload
    ) {
    }

    public record Head(
            String subscriptionId,
            Instant timestamp,
            boolean locked,
            List<ObjectId> eventIds
    ) {
    }

    interface IDDecoder<ID> {
        ID decode(String id);
    }

    interface IDCoder<ID> {
        String encode(ID id);
    }
}
