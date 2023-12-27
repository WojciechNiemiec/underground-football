package football.underground.wallet;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletAccessorFactory;

public class WalletAccessorFactoryImpl implements WalletAccessorFactory {
    @Override
    public WalletAccessor create(EventRepository<UUID> walletEventRepository) {
        return new WalletAccessorImpl(walletEventRepository);
    }
}
