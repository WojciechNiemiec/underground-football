package football.underground.game.spi;

import static football.underground.game.api.GameProjection.GamePage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import football.underground.game.api.GameProjection;

class InMemoryGameInfoRepository implements GameInfoRepository {
    private final Map<UUID, GameProjection.GameInfo> games = new HashMap<>();

    @Override
    public void save(GameProjection.GameInfo game) {
        games.put(game.getGameId(), game);
    }

    @Override
    public void delete(UUID gameId) {
        games.remove(gameId);
    }

    @Override
    public GameProjection.GameInfo getGame(UUID gameId) {
        return games.get(gameId);
    }

    @Override
    public GamePage getGames(int page, int pageSize, String state, UUID locationId, UUID organizerId) {
        var content = games.values()
                .stream()
                .filter(game -> match(game, state, locationId, organizerId))
                .skip((long) page * pageSize)
                .limit(pageSize)
                .toList();

        var count = games.values()
                .stream()
                .filter(game -> match(game, state, locationId, organizerId))
                .count();

        return new GamePage(page, pageSize, count / pageSize, count, content);
    }

    private boolean match(GameProjection.GameInfo game, String state, UUID locationId, UUID organizerId) {
        return (state == null || state.equals(game.getState()))
               && (locationId == null || locationId.equals(game.getLocationId()))
               && (organizerId == null || organizerId.equals(game.getOrganizerId()));
    }
}
