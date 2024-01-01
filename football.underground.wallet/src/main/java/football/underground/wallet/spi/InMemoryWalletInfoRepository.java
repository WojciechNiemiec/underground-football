package football.underground.wallet.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import football.underground.wallet.api.WalletProjection.WalletInfo;

class InMemoryWalletInfoRepository implements WalletInfoRepository {
    private final Map<UUID, WalletInfo> wallets = new HashMap<>();

    @Override
    public Optional<WalletInfo> getWallet(UUID accountId) {
        return Optional.ofNullable(wallets.get(accountId));
    }

    @Override
    public void save(WalletInfo walletInfo) {
        wallets.put(walletInfo.accountId(), walletInfo);
    }

    @Override
    public void delete(UUID accountId) {
        wallets.remove(accountId);
    }
}
