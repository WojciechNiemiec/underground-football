package football.underground.wallet;

import java.util.UUID;

import football.underground.eventsourcing.AggregateContext;
import football.underground.eventsourcing.EventRepository;
import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.wallet.api.ChargeProxy;
import football.underground.wallet.api.MoneyRegistrar;
import football.underground.wallet.api.WalletAccessor;

class WalletAccessorImpl implements WalletAccessor {
    private static final EventSourcingConfiguration<Wallet, UUID> CONFIGURATION = new AggregateConfiguration();

    private final EventRepository<UUID> walletEventRepository;

    WalletAccessorImpl(EventRepository<UUID> walletEventRepository) {
        this.walletEventRepository = walletEventRepository;
    }

    @Override
    public ChargeProxy chargeProxy(UUID sourceAccount) {
        return new AggregateContext<Wallet, ChargeProxy, UUID>(walletEventRepository)
                .load(sourceAccount, CONFIGURATION, Wallet::new, ChargeProxy.class);
    }

    @Override
    public MoneyRegistrar moneyRegistrar(UUID targetAccount) {
        return new AggregateContext<Wallet, MoneyRegistrar, UUID>(walletEventRepository)
                .load(targetAccount, CONFIGURATION, Wallet::new, MoneyRegistrar.class);
    }
}
