package football.underground.application.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.ReplaceOptions;

import football.underground.game.api.GameProjection;
import football.underground.game.api.GameProjection.GamePage;
import football.underground.game.spi.GameInfoRepository;

class MongoGameInfoRepository implements GameInfoRepository {
    private static final String COLLECTION = "games";
    private static final int COUNT_LIMIT = 10_000;

    private final MongoCollection<GameProjection.GameInfo> gameCollection;

    public MongoGameInfoRepository(MongoDatabase database) {
        this.gameCollection = database.getCollection(COLLECTION, GameProjection.GameInfo.class);
    }

    @Override
    public void save(GameProjection.GameInfo game) {
        gameCollection.replaceOne(gameIdEquals(game.getGameId()), game, new ReplaceOptions().upsert(true));
    }

    @Override
    public void delete(UUID gameId) {
        gameCollection.deleteOne(gameIdEquals(gameId));
    }

    @Override
    public GameProjection.GameInfo getGame(UUID gameId) {
        return Objects.requireNonNull(gameCollection.find(gameIdEquals(gameId)).first());
    }

    @Override
    public GamePage getGames(int page, int pageSize, String state, UUID locationId, UUID organizerId) {
        BasicDBObject match = new BasicDBObject();
        if (state != null) {
            match.append("state", state);
        }
        if (locationId != null) {
            match.append("locationId", locationId);
        }
        if (organizerId != null) {
            match.append("organizerId", organizerId);
        }

        List<GameProjection.GameInfo> gameInfos = gameCollection.aggregate(List.of(
                new BasicDBObject("$match", match),
                new BasicDBObject("$sort", new BasicDBObject("created", -1)),
                new BasicDBObject("$skip", page * pageSize),
                new BasicDBObject("$limit", pageSize)
        )).into(new ArrayList<>());

        var count = gameCollection.countDocuments(match, new CountOptions().limit(COUNT_LIMIT));

        return new GamePage(page, pageSize, count / pageSize, count, gameInfos);
    }

    private static BasicDBObject gameIdEquals(UUID gameId) {
        return new BasicDBObject("gameId", gameId);
    }
}
