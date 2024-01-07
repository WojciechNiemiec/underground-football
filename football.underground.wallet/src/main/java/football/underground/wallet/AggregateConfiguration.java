package football.underground.wallet;

import java.util.UUID;

import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.eventsourcing.EventSourcingSubscriber;
import football.underground.wallet.event.ChargeFailed;
import football.underground.wallet.event.ChargeInitiated;
import football.underground.wallet.event.ChargePaid;
import football.underground.wallet.event.MoneyRegistered;

class AggregateConfiguration implements EventSourcingConfiguration<Wallet, UUID> {
    @Override
    public void registerHandlers(EventSourcingSubscriber<Wallet, UUID> subscriber) {
        subscriber.subscribe(MoneyRegistered.class, Wallet::handle);
        subscriber.subscribe(ChargeInitiated.class, Wallet::handle);
        subscriber.subscribe(ChargePaid.class, Wallet::handle);
        subscriber.subscribe(ChargeFailed.class, Wallet::handle);
    }
}
