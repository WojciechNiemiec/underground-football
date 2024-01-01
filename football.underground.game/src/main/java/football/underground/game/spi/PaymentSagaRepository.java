package football.underground.game.spi;

import java.util.UUID;

public interface PaymentSagaRepository {
    void save(PaymentSaga paymentSaga);

    PaymentSaga load(UUID transactionId);

    void delete(UUID transactionId);

    static PaymentSagaRepository inMemory() {
        return new InMemoryPaymentSagaRepository();
    }
}
