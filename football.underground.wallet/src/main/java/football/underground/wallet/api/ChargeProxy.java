package football.underground.wallet.api;

import java.util.UUID;

public interface ChargeProxy {
    void charge(UUID transactionId, UUID creditorId, MoneyAmount moneyAmount, boolean debtAllowed);
}
