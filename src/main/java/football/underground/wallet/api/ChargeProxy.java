package football.underground.wallet.api;

import java.util.UUID;

public interface ChargeProxy {
    void charge(UUID transactionId, UUID debtorId, UUID creditorId, MoneyAmount moneyAmount, boolean debtAllowed);
}
