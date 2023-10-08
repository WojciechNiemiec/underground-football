package football.underground.game;

import football.underground.eventsourcing.Appender;
import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PaymentInitialized;
import football.underground.game.event.PlayerMarkedReserve;
import football.underground.wallet.api.MoneyAmount;
import football.underground.game.event.PlayerConfirmed;

import java.time.Instant;
import java.util.UUID;

import static football.underground.game.Player.PaymentStatus.INITIALIZED;
import static football.underground.game.Player.PaymentStatus.UNPAID;
import static football.underground.game.Player.PlayStatus.*;

class Player {
    private final Appender stream;
    private final UUID playerId;

    private PlayStatus playStatus;
    private PaymentStatus paymentStatus;
    private Instant signedAt;

    Player(Appender stream, UUID playerId, Instant signedAt) {
        this.stream = stream;
        this.playerId = playerId;
        this.signedAt = signedAt;
        playStatus = PENDING;
        paymentStatus = UNPAID;
    }

    boolean isConfirmed() {
        return playStatus == CONFIRMED;
    }

    boolean isReserve() {
        return playStatus == RESERVE;
    }

    void confirm() {
        stream.append(new PlayerConfirmed(playerId));
    }

    void markReserve() {
        stream.append(new PlayerMarkedReserve(playerId));
    }

    void initializePayment(MoneyAmount fee, boolean debtAllowed) {
        if (playStatus != CONFIRMED) {
            throw new IllegalStateException("Player must be confirmed to play");
        }
        switch (paymentStatus) {
            case UNPAID -> stream.append(new PaymentInitialized(playerId, fee, debtAllowed));
            case INITIALIZED -> {}
            case COMPLETED -> throw new IllegalStateException("Game has already been paid");
        }
    }

    void confirmPayment() {
        switch (paymentStatus) {
            case UNPAID -> throw new IllegalStateException("Payment must be initialized");
            case INITIALIZED -> stream.append(new PaymentConfirmed(playerId));
            case COMPLETED -> {}
        }
    }

    void handlePlayerConfirmed() {
        playStatus = CONFIRMED;
    }

    void handlePlayerMarkedReserve() {
        playStatus = RESERVE;
    }

    void handlePaymentInitialized() {
        paymentStatus = INITIALIZED;
    }

    void handlePaymentConfirmed() {
    }

    public Instant signedAt() {
        return signedAt;
    }

    public enum PlayStatus {
        PENDING, CONFIRMED, RESERVE
    }

    enum PaymentStatus {
        UNPAID, INITIALIZED, COMPLETED
    }
}
