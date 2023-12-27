package football.underground.game.spi;

import java.util.UUID;

public record PaymentSaga(UUID transactionId, UUID gameId, UUID playerId) {
}
