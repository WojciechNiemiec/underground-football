package football.underground.wallet.api;

import java.util.List;
import java.util.UUID;

public interface WalletProjection {

    WalletInfo getWallet(UUID accountId);

    record WalletInfo(
        UUID accountId,
        List<Balance> balances) {
    }

    record Balance(UUID creditorId, MoneyAmount amount) {
    }
}
