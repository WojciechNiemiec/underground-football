package football.underground.wallet.spi;

import java.util.Optional;
import java.util.UUID;

import football.underground.wallet.api.WalletProjection.WalletInfo;

public interface WalletInfoRepository {
    Optional<WalletInfo> getWallet(UUID accountId);

    void save(WalletInfo walletInfo); // enforce idempotency

    void delete(UUID accountId);

    static WalletInfoRepository inMemory() {
        return new InMemoryWalletInfoRepository();
    }
}
