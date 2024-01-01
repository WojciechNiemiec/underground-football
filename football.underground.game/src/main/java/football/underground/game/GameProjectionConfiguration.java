package football.underground.game;

import java.util.UUID;

import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.eventsourcing.EventSourcingSubscriber;
import football.underground.game.event.GameCancelled;
import football.underground.game.event.GameConfirmed;
import football.underground.game.event.GameFinished;
import football.underground.game.event.GameInitialized;
import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PaymentInitialized;
import football.underground.game.event.PlayerConfirmed;
import football.underground.game.event.PlayerMarkedReserve;
import football.underground.game.event.PlayerSignedOut;
import football.underground.game.event.PlayerSignedUp;
import football.underground.game.event.TeamsDefined;

class GameProjectionConfiguration implements EventSourcingConfiguration<GameProjectionImpl, UUID> {
    @Override
    public void registerHandlers(EventSourcingSubscriber<GameProjectionImpl, UUID> subscriber) {
        subscriber.subscribeWithMeta(GameInitialized.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(GameConfirmed.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(
                GameCancelled.class,
                (service, event, id, date) -> service.handleGameCancelled(id)
        );
        subscriber.subscribeWithMeta(GameFinished.class, (service, event, id, date) -> service.handleGameFinished(id));
        subscriber.subscribeWithMeta(PlayerSignedUp.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(PlayerSignedOut.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(PlayerConfirmed.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(
                PlayerMarkedReserve.class,
                (service, event, id, date) -> service.handle(event, id)
        );
        subscriber.subscribeWithMeta(PaymentInitialized.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(PaymentConfirmed.class, (service, event, id, date) -> service.handle(event, id));
        subscriber.subscribeWithMeta(TeamsDefined.class, (service, event, id, date) -> service.handle(event, id));
    }
}
