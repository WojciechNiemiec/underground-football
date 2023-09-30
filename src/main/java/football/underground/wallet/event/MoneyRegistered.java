package football.underground.wallet.event;

import java.util.UUID;

import football.underground.wallet.api.MoneyAmount;

public record MoneyRegistered(UUID creditorId, MoneyAmount amount) {
}
