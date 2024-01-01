package football.underground.wallet.api;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.wallet.spi.WalletInfoRepository;

public interface WalletServicesFactory {
    WalletAccessor walletAccessor(EventRepository<UUID> walletEventRepository);

    WalletProjection walletProjection(
            EventRepository<UUID> walletEventRepository,
            WalletInfoRepository walletInfoRepository
    );
}
