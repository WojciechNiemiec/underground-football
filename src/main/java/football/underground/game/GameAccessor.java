package football.underground.game;

import java.util.UUID;
import java.util.function.Consumer;

import football.underground.eventsourcing.EventStream;
import football.underground.eventsourcing.EventStreamRepository;
import football.underground.game.api.GameInitializer;
import football.underground.game.api.GameManager;
import football.underground.game.api.PlayerManager;

class GameAccessor {
    private static final EventStream.Configuration<Game> CONFIGURATION = new AggregateConfiguration();

    private final EventStreamRepository<Game, UUID> gameEventStreamRepository;

    GameAccessor(EventStreamRepository<Game, UUID> walletEventStreamRepository) {
        this.gameEventStreamRepository = walletEventStreamRepository;
    }

    void init(Consumer<GameInitializer> action) {
        EventStream<Game> eventStream = new EventStream<>(UUID.randomUUID(), CONFIGURATION);
        action.accept(new Game.Initializer(eventStream));
        gameEventStreamRepository.save(eventStream);
    }

    void manage(UUID gameId, Consumer<GameManager> action) {
        EventStream<Game> eventStream = new EventStream<>(gameId, CONFIGURATION);
        Game game = eventStream.load(Game::new);
        action.accept(game);
        gameEventStreamRepository.save(eventStream);
    }

    void managePlayer(UUID gameId, Consumer<PlayerManager> action) {
        EventStream<Game> eventStream = new EventStream<>(gameId, CONFIGURATION);
        Game game = eventStream.load(Game::new);
        action.accept(game);
        gameEventStreamRepository.save(eventStream);
    }
}
