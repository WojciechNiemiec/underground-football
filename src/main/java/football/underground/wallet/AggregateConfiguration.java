package football.underground.wallet;

import football.underground.eventsourcing.EventStream;
import football.underground.wallet.event.ChargeInitiated;
import football.underground.wallet.event.ChargePaid;
import football.underground.wallet.event.MoneyRegistered;

class AggregateConfiguration implements EventStream.Configuration<Wallet> {

    @Override
    public void registerHandlers(EventStream.Subscriber<Wallet> subscriber) {
        subscriber.subscribe(ChargeInitiated.class, Wallet::handle);
        subscriber.subscribe(ChargePaid.class, Wallet::handle);
        subscriber.subscribe(MoneyRegistered.class, Wallet::handle);
    }
}
