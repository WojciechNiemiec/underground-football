package football.underground.wallet;

import java.util.UUID;

import football.underground.eventsourcing.EventStream;
import football.underground.eventsourcing.EventStreamRepository;
import football.underground.wallet.api.ChargeProxy;
import football.underground.wallet.api.MoneyAmount;

class WalletService implements ChargeProxy {
    private static final EventStream.Configuration<Wallet> CONFIGURATION = new AggregateConfiguration();

    private final EventStreamRepository<Wallet, UUID> walletEventStreamRepository;

    WalletService(EventStreamRepository<Wallet, UUID> walletEventStreamRepository) {
        this.walletEventStreamRepository = walletEventStreamRepository;
    }

    public void charge(UUID transactionId, UUID debtorId, UUID creditorId, MoneyAmount amount, boolean debtAllowed) {
        EventStream<Wallet> eventStream = walletEventStreamRepository.load(debtorId, CONFIGURATION);
        Wallet wallet = eventStream.load(Wallet::new);
        wallet.charge(transactionId, creditorId, amount, debtAllowed);
        walletEventStreamRepository.save(eventStream);
    }
}
