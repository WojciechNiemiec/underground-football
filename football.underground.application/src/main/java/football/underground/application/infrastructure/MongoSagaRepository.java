package football.underground.application.infrastructure;

import java.util.Objects;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import football.underground.game.spi.PaymentSaga;
import football.underground.game.spi.PaymentSagaRepository;

class MongoSagaRepository implements PaymentSagaRepository {
    private static final String COLLECTION_NAME = "payment-sagas";

    private final MongoCollection<PaymentSaga> sagaCollection;

    MongoSagaRepository(MongoDatabase database) {
        sagaCollection = database.getCollection(COLLECTION_NAME, PaymentSaga.class);
    }

    @Override
    public void save(PaymentSaga paymentSaga) {
        sagaCollection.replaceOne(
                transactionIdEquals(paymentSaga.transactionId()),
                paymentSaga,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public PaymentSaga load(UUID transactionId) {
        return Objects.requireNonNull(sagaCollection.find(transactionIdEquals(transactionId)).first());
    }

    @Override
    public void delete(UUID transactionId) {
        sagaCollection.deleteOne(transactionIdEquals(transactionId));
    }

    private static BasicDBObject transactionIdEquals(UUID transactionId) {
        return new BasicDBObject("transactionId", transactionId);
    }
}
