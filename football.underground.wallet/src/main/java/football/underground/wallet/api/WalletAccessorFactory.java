package football.underground.wallet.api;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;

public interface WalletAccessorFactory {
    WalletAccessor create(EventRepository<UUID> walletEventRepository);
}
