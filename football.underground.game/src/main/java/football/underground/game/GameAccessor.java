package football.underground.game;

import java.util.UUID;
import java.util.function.Consumer;

import football.underground.eventsourcing.EventStream;
import football.underground.eventsourcing.EventStreamLoader;
import football.underground.game.api.GameInitializer;
import football.underground.game.api.GameManager;
import football.underground.game.api.PlayerManager;

class GameAccessor {
    private static final EventStream.Configuration<Game> CONFIGURATION = new AggregateConfiguration();

    private final EventStreamLoader<Game, UUID> gameEventStreamLoader;

    GameAccessor(EventStreamLoader<Game, UUID> walletEventStreamLoader) {
        this.gameEventStreamLoader = walletEventStreamLoader;
    }

    void init(Consumer<GameInitializer> action) {
        EventStream<UUID, Game> eventStream = gameEventStreamLoader.load(UUID.randomUUID(), CONFIGURATION);
        action.accept(new Game.Initializer(eventStream));
        gameEventStreamLoader.save(eventStream);
    }

    void manage(UUID gameId, Consumer<GameManager> action) {
        EventStream<UUID, Game> eventStream = gameEventStreamLoader.load(gameId, CONFIGURATION);
        Game game = eventStream.load(Game::new);
        action.accept(game);
        gameEventStreamLoader.save(eventStream);
    }

    void managePlayer(UUID gameId, Consumer<PlayerManager> action) {
        EventStream<UUID, Game> eventStream = gameEventStreamLoader.load(gameId, CONFIGURATION);
        Game game = eventStream.load(Game::new);
        action.accept(game);
        gameEventStreamLoader.save(eventStream);
    }
}
