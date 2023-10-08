package football.underground.wallet;

import java.util.UUID;

import football.underground.eventsourcing.EventStream;
import football.underground.eventsourcing.EventStreamLoader;
import football.underground.wallet.api.ChargeProxy;
import football.underground.wallet.api.MoneyAmount;
import football.underground.wallet.api.MoneyRegistrar;

class WalletService implements ChargeProxy, MoneyRegistrar {
    private static final EventStream.Configuration<Wallet> CONFIGURATION = new AggregateConfiguration();

    private final EventStreamLoader<Wallet, UUID> walletEventStreamLoader;

    WalletService(EventStreamLoader<Wallet, UUID> walletEventStreamLoader) {
        this.walletEventStreamLoader = walletEventStreamLoader;
    }

    @Override
    public void charge(UUID transactionId, UUID debtorId, UUID creditorId, MoneyAmount amount, boolean debtAllowed) {
        EventStream<UUID, Wallet> eventStream = walletEventStreamLoader.load(debtorId, CONFIGURATION);
        Wallet wallet = eventStream.load(Wallet::new);
        wallet.charge(transactionId, creditorId, amount, debtAllowed);
        walletEventStreamLoader.save(eventStream);
    }

    @Override
    public void register(UUID sourceAccount, UUID targetAccount, MoneyAmount amount) {
        EventStream<UUID, Wallet> eventStream = walletEventStreamLoader.load(targetAccount, CONFIGURATION);
        Wallet wallet = eventStream.load(Wallet::new);
        wallet.registerMoney(sourceAccount, amount);
        walletEventStreamLoader.save(eventStream);
    }
}
