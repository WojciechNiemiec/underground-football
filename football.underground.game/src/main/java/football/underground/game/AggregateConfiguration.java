package football.underground.game;

import football.underground.eventsourcing.EventStream;
import football.underground.game.event.*;

class AggregateConfiguration implements EventStream.Configuration<Game> {

    @Override
    public void registerHandlers(EventStream.Subscriber<Game> subscriber) {
        subscriber.subscribe(GameInitialized.class, Game::handle);
        subscriber.subscribe(GameConfirmed.class, Game::handle);
        subscriber.subscribe(GameCancelled.class, (game, event) -> game.handleGameCancelled());
        subscriber.subscribe(GameFinished.class, (game, event) -> game.handleGameFinished());
        subscriber.subscribeWithMeta(PlayerSignedUp.class, Game::handle);
        subscriber.subscribe(PlayerSignedOut.class, Game::handle);
        subscriber.subscribe(PlayerConfirmed.class, Game::handle);
        subscriber.subscribe(PlayerMarkedReserve.class, Game::handle);
        subscriber.subscribe(TeamsDefined.class, Game::handle);
        subscriber.subscribe(PaymentInitialized.class, Game::handle);
        subscriber.subscribe(PaymentConfirmed.class, Game::handle);
    }
}
