package football.underground.game.spi;

import static football.underground.game.api.GameProjection.GameInfo;
import static football.underground.game.api.GameProjection.GamePage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class InMemoryGameProjectionRepository implements GameProjectionRepository {
    private final Map<UUID, GameInfo> games = new HashMap<>();

    @Override
    public void save(GameInfo game) {
        games.put(game.getId(), game);
    }

    @Override
    public void delete(UUID gameId) {
        games.remove(gameId);
    }

    @Override
    public GameInfo getGame(UUID gameId) {
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

    private boolean match(GameInfo game, String state, UUID locationId, UUID organizerId) {
        return (state == null || state.equals(game.getState()))
               && (locationId == null || locationId.equals(game.getLocationId()))
               && (organizerId == null || organizerId.equals(game.getOrganizerId()));
    }
}
