package football.underground.game;

import java.util.UUID;

import football.underground.eventsourcing.EventSourcingConfiguration;
import football.underground.eventsourcing.AggregateContext;
import football.underground.eventsourcing.EventRepository;
import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameInitializer;
import football.underground.game.api.GameManager;
import football.underground.game.api.PlayerManager;

class GameAccessorImpl implements GameAccessor {
    private static final EventSourcingConfiguration<Game, UUID> CONFIGURATION = new AggregateConfiguration();

    private final EventRepository<UUID> gameEventRepository;

    public GameAccessorImpl(EventRepository<UUID> gameEventRepository) {
        this.gameEventRepository = gameEventRepository;
    }

    @Override
    public GameInitializer gameInitializer(UUID gameId) {
        return new AggregateContext<Game, GameInitializer, UUID>(gameEventRepository)
                .load(gameId, CONFIGURATION, Game::new, GameInitializer.class);
    }

    @Override
    public GameManager gameManager(UUID gameId) {
        return new AggregateContext<Game, GameManager, UUID>(gameEventRepository)
                .load(gameId, CONFIGURATION, Game::new, GameManager.class);
    }

    @Override
    public PlayerManager playerManager(UUID gameId) {
        return new AggregateContext<Game, PlayerManager, UUID>(gameEventRepository)
                .load(gameId, CONFIGURATION, Game::new, PlayerManager.class);
    }
}
