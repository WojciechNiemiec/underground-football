package football.underground.wallet.api;

import java.util.UUID;

public interface MoneyRegistrar {
    void register(UUID sourceAccount, MoneyAmount amount);
}
