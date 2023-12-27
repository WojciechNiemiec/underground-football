package football.underground.game;

import java.util.UUID;

import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.eventsourcing.EventSourcingSubscriber;
import football.underground.game.event.*;

class AggregateConfiguration implements EventSourcingConfiguration<Game, UUID> {
    @Override
    public void registerHandlers(EventSourcingSubscriber<Game, UUID> subscriber) {
        subscriber.subscribe(GameInitialized.class, Game::handle);
        subscriber.subscribe(GameConfirmed.class, Game::handle);
        subscriber.subscribe(GameCancelled.class, (game, event) -> game.handleGameCancelled());
        subscriber.subscribe(GameFinished.class, (game, event) -> game.handleGameFinished());
        subscriber.subscribeWithMeta(PlayerSignedUp.class, (game, event, id, date) -> game.handle(event, date));
        subscriber.subscribe(PlayerSignedOut.class, Game::handle);
        subscriber.subscribe(PlayerConfirmed.class, Game::handle);
        subscriber.subscribe(PlayerMarkedReserve.class, Game::handle);
        subscriber.subscribe(TeamsDefined.class, Game::handle);
        subscriber.subscribe(PaymentInitialized.class, Game::handle);
        subscriber.subscribe(PaymentConfirmed.class, Game::handle);
    }
}
