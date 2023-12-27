package football.underground.wallet.api;

import java.util.UUID;

public interface WalletAccessor {
    ChargeProxy chargeProxy(UUID sourceAccount);
    MoneyRegistrar moneyRegistrar(UUID targetAccount);
}
