package football.underground.game;

import java.util.UUID;

import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.eventsourcing.EventSourcingSubscriber;
import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PaymentInitialized;
import football.underground.game.event.PlayerSignedOut;
import football.underground.wallet.event.ChargeFailed;
import football.underground.wallet.event.ChargePaid;

class PlayerPaymentServiceConfiguration implements EventSourcingConfiguration<PlayerPaymentService, UUID> {
    @Override
    public void registerHandlers(EventSourcingSubscriber<PlayerPaymentService, UUID> subscriber) {
        subscriber.subscribeWithMeta(PaymentInitialized.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribe(ChargePaid.class, PlayerPaymentService::handle);
        subscriber.subscribe(ChargeFailed.class, PlayerPaymentService::handle);
        subscriber.subscribeWithMeta(PaymentConfirmed.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(PlayerSignedOut.class, (service, event, id, date) -> service.handle(event, id));
    }
}
