package football.underground.game.event;

import football.underground.wallet.api.MoneyAmount;

import java.util.UUID;

public record PaymentInitialized(UUID playerId, UUID organiserId, MoneyAmount charge, boolean debtAllowed) {
}
