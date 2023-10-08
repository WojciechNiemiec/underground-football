package football.underground.wallet.event;

import football.underground.wallet.api.MoneyAmount;

import java.util.UUID;

public record ChargeInitiated(UUID transactionId, UUID creditorId, MoneyAmount amount, boolean debtAllowed) {
}
