package football.underground.game;

import static football.underground.game.api.GameProjection.PaymentStatus.COMPLETED;
import static football.underground.game.api.GameProjection.PaymentStatus.INITIALIZED;
import static football.underground.game.api.GameProjection.PaymentStatus.UNPAID;
import static football.underground.game.api.GameProjection.PlayStatus.CONFIRMED;
import static football.underground.game.api.GameProjection.PlayStatus.PENDING;
import static football.underground.game.api.GameProjection.PlayStatus.RESERVE;

import java.util.HashMap;
import java.util.UUID;

import football.underground.game.api.GameProjection;
import football.underground.game.event.GameConfirmed;
import football.underground.game.event.GameInitialized;
import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PaymentInitialized;
import football.underground.game.event.PlayerConfirmed;
import football.underground.game.event.PlayerMarkedReserve;
import football.underground.game.event.PlayerSignedOut;
import football.underground.game.event.PlayerSignedUp;
import football.underground.game.event.TeamsDefined;
import football.underground.game.spi.GameInfoRepository;

class GameProjectionImpl implements GameProjection {
    private final GameInfoRepository repository;

    GameProjectionImpl(GameInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public GameInfo getGame(UUID gameId) {
        return repository.getGame(gameId);
    }

    @Override
    public GamePage getGames(int page, int pageSize, String state, UUID locationId, UUID organizerId) {
        return repository.getGames(page, pageSize, state, locationId, organizerId);
    }

    void handle(GameInitialized event, UUID gameId) {
        var details = new GameInfo();

        details.setGameId(gameId);
        details.setOrganizerId(event.organizerId());
        details.setLocationId(event.locationId());
        details.setDate(event.date());
        details.setDuration(event.duration());
        details.setSettlementStrategy(event.settlementStrategy());
        details.setMinPlayers(event.minPlayers());
        details.setMaxPlayers(event.maxPlayers());
        details.setPlayers(new HashMap<>());
        details.setState("INITIALIZED");

        repository.save(details);
    }

    void handle(GameConfirmed event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.setState("CONFIRMED");
        game.setFee(event.fee());

        repository.save(game);
    }

    void handleGameCancelled(UUID gameId) {
        var game = repository.getGame(gameId);

        game.setState("CANCELLED");

        repository.save(game);
    }

    void handleGameFinished(UUID gameId) {
        var game = repository.getGame(gameId);

        game.setState("FINISHED");

        repository.save(game);
    }

    void handle(PlayerSignedUp event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.getPlayers().put(
                event.playerId().toString(),
                new PlayerInfo(event.playerId().toString(), PENDING, UNPAID)
        );

        repository.save(game);
    }

    void handle(PlayerSignedOut event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.getPlayers().remove(event.playerId());

        repository.save(game);
    }

    void handle(PlayerConfirmed event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.getPlayers().computeIfPresent(
                event.playerId().toString(),
                (uuid, playerInfo) -> new PlayerInfo(uuid, CONFIRMED, playerInfo.paymentStatus())
        );

        repository.save(game);
    }

    void handle(PlayerMarkedReserve event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.getPlayers().computeIfPresent(
                event.playerId().toString(),
                (uuid, playerInfo) -> new PlayerInfo(uuid, RESERVE, playerInfo.paymentStatus())
        );

        repository.save(game);
    }

    void handle(PaymentInitialized event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.getPlayers().computeIfPresent(
                event.playerId().toString(),
                (uuid, playerInfo) -> new PlayerInfo(uuid, playerInfo.playStatus(), INITIALIZED)
        );

        repository.save(game);
    }

    void handle(PaymentConfirmed event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.getPlayers().computeIfPresent(
                event.playerId().toString(),
                (uuid, playerInfo) -> new PlayerInfo(uuid, playerInfo.playStatus(), COMPLETED)
        );

        repository.save(game);
    }

    void handle(TeamsDefined event, UUID gameId) {
        var game = repository.getGame(gameId);

        game.setHomeTeamId(event.homeTeamId());
        game.setGuestTeamId(event.guestTeamId());

        repository.save(game);
    }
}
