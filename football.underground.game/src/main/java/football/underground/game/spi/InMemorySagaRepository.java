package football.underground.game.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class InMemorySagaRepository implements SagaRepository {
    private final Map<UUID, PaymentSaga> sagas = new HashMap<>();

    @Override
    public void save(PaymentSaga saga) {
        sagas.put(saga.transactionId(), saga);
    }

    @Override
    public PaymentSaga load(UUID transactionId) {
        return sagas.get(transactionId);
    }

    @Override
    public void delete(UUID transactionId) {
        sagas.remove(transactionId);
    }
}
