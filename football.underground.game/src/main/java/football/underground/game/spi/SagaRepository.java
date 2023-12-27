package football.underground.game.spi;

import java.util.UUID;

public interface SagaRepository {
    void save(PaymentSaga paymentSaga);

    PaymentSaga load(UUID transactionId);

    void delete(UUID transactionId);

    static SagaRepository inMemory() {
        return new InMemorySagaRepository();
    }
}
