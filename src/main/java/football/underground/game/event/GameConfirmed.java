package football.underground.game.event;

import football.underground.wallet.api.MoneyAmount;

public record GameConfirmed(MoneyAmount fee) {
}
