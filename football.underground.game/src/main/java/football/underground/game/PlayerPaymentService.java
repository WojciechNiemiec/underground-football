package football.underground.game;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import football.underground.game.api.GameAccessor;
import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PaymentInitialized;
import football.underground.game.event.PlayerSignedOut;
import football.underground.game.spi.PaymentSaga;
import football.underground.game.spi.SagaRepository;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.event.ChargeFailed;
import football.underground.wallet.event.ChargePaid;

class PlayerPaymentService {
    private final SagaRepository sagaRepository;
    private final GameAccessor gameAccessor;
    private final WalletAccessor walletAccessor;

    PlayerPaymentService(SagaRepository sagaRepository, GameAccessor gameAccessor, WalletAccessor walletAccessor) {
        this.sagaRepository = sagaRepository;
        this.gameAccessor = gameAccessor;
        this.walletAccessor = walletAccessor;
    }

    void handle(PaymentInitialized event, UUID gameId) {
        UUID transactionId = transactionId(gameId, event.playerId());
        var saga = new PaymentSaga(transactionId, gameId, event.playerId());
        sagaRepository.save(saga);
        walletAccessor.chargeProxy(event.playerId())
                .charge(transactionId, event.organiserId(), event.charge(), event.debtAllowed());
    }

    void handle(ChargePaid event) {
        PaymentSaga saga = sagaRepository.load(event.transactionId());
        gameAccessor.gameManager(saga.gameId()).confirmPayment(saga.playerId());
    }

    void handle(ChargeFailed event) {
        PaymentSaga saga = sagaRepository.load(event.transactionId());
        gameAccessor.playerManager(saga.gameId()).signOutPlayer(saga.playerId());
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

    /**
     * Generates non-random transaction id based on game and player ids.
     * Method guarantees that same transaction id is always generated for the same game and player.
     * This is needed to avoid duplicate transactions in case of replaying events.
     * @param gameId - game identifier
     * @param playerId - identifier of player to be charged
     * @return transaction identifier
     */
    private UUID transactionId(UUID gameId, UUID playerId) {
        int hash = Objects.hash(gameId, playerId);
        return UUID.nameUUIDFromBytes(String.valueOf(hash).getBytes(StandardCharsets.UTF_8));
    }
}
