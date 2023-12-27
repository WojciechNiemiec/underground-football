package football.underground.game.api;

import java.util.UUID;

public interface GameAccessor {
    GameInitializer gameInitializer(UUID gameId);
    GameManager gameManager(UUID gameId);
    PlayerManager playerManager(UUID gameId);
}
