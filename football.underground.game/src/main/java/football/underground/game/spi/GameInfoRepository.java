package football.underground.game.spi;

import java.util.UUID;

import football.underground.game.api.GameProjection.GameInfo;
import football.underground.game.api.GameProjection.GamePage;

public interface GameInfoRepository {
    void save(GameInfo game);

    void delete(UUID gameId);

    GameInfo getGame(UUID gameId);

    GamePage getGames(int page, int pageSize, String state, UUID locationId, UUID organizerId);

    static GameInfoRepository inMemory() {
        return new InMemoryGameInfoRepository();
    }
}
