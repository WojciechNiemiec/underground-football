package football.underground.game;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PaymentInitialized;
import football.underground.game.event.PlayerSignedOut;
import football.underground.wallet.api.ChargeProxy;
import football.underground.wallet.event.ChargeFailed;
import football.underground.wallet.event.ChargePaid;

class PlayerPaymentService {
    private final SagaRepository sagaRepository;
    private final GameAccessor gameAccessor;
    private final ChargeProxy chargeProxy;

    PlayerPaymentService(SagaRepository sagaRepository, GameAccessor gameAccessor, ChargeProxy chargeProxy) {
        this.sagaRepository = sagaRepository;
        this.gameAccessor = gameAccessor;
        this.chargeProxy = chargeProxy;
    }

    void handle(PaymentInitialized event, UUID gameId, UUID organiserId) {
        UUID transactionId = transactionId(gameId, event.playerId());
        var saga = new Saga(transactionId, gameId, event.playerId());
        sagaRepository.save(saga);
        chargeProxy.charge(transactionId, event.playerId(), organiserId, event.charge(), event.debtAllowed());
    }

    void handle(ChargePaid event) {
        Saga saga = sagaRepository.load(event.transactionId());
        gameAccessor.manage(saga.gameId(), gameManager -> gameManager.confirmPayment(saga.playerId()));
    }

    void handle(ChargeFailed event) {
        Saga saga = sagaRepository.load(event.transactionId());
        gameAccessor.managePlayer(saga.gameId(), gameManager -> gameManager.signOutPlayer(saga.playerId()));
    }

    void handle(PaymentConfirmed event, UUID gameId) {
        endSaga(gameId, event.playerId());
    }

    void handle(PlayerSignedOut event, UUID gameId) {
        endSaga(gameId, event.playerId());
    }

    private void endSaga(UUID gameId, UUID playerId) {
        UUID transactionId = transactionId(gameId, playerId);
        sagaRepository.delete(transactionId);
    }

    private UUID transactionId(UUID gameId, UUID playerId) {
        int hash = Objects.hash(gameId, playerId);
        return UUID.nameUUIDFromBytes(String.valueOf(hash).getBytes(StandardCharsets.UTF_8));
    }

    interface SagaRepository {
        void save(Saga saga);
        Saga load(UUID transactionId);
        void delete(UUID transactionId);
    }

    record Saga(UUID transactionId, UUID gameId, UUID playerId) {
    }
}
