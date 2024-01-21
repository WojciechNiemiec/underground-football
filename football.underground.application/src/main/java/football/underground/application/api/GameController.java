package football.underground.application.api;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameProjection;
import football.underground.game.api.GameProjection.GamePage;
import football.underground.game.api.SettlementStrategy;
import football.underground.wallet.api.MoneyAmount;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;

@Controller("/game-api/v1-beta")
class GameController {
    private final GameAccessor gameAccessor;
    private final GameProjection gameProjection;

    public GameController(GameAccessor gameAccessor, GameProjection gameProjection) {
        this.gameAccessor = gameAccessor;
        this.gameProjection = gameProjection;
    }

    @Get("/games")
    GamePage lookup(
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "100") int pageSize,
            @QueryValue String state,
            @QueryValue UUID locationId,
            @QueryValue UUID organizerId
    ) {
        return gameProjection.getGames(page, pageSize, state, locationId, organizerId);
    }

    @Post("/games")
    @Status(HttpStatus.CREATED)
    GameResponse create(@Body GameRequest request, @Header("X-Identity-Id") UUID identityId) {
        UUID gameId = UUID.randomUUID();

        gameAccessor.gameInitializer(gameId).initialize(
                identityId,
                request.locationId(),
                request.date(),
                request.duration(),
                request.settlementStrategy(),
                request.minPlayers(),
                request.maxPlayers()
        );

        return new GameResponse(gameId);
    }

    @Post("/games/{gameId}/cancellations")
    @Status(HttpStatus.ACCEPTED)
    void cancel(@PathVariable("gameId") UUID gameId, @Body GameCancellation request) {
        gameAccessor.gameManager(gameId).cancel(request.reason());
    }

    @Post("/games/{gameId}/confirmations")
    @Status(HttpStatus.ACCEPTED)
    void confirm(@PathVariable("gameId") UUID gameId, @Body GameConfirmation confirmation) {
        gameAccessor.gameManager(gameId).confirm(confirmation.fee());
    }

    @Post("/games/{gameId}/players/{playerId}/confirmations")
    @Status(HttpStatus.ACCEPTED)
    void confirmPlayer(
            @PathVariable("gameId") UUID gameId,
            @PathVariable("playerId") UUID playerId,
            @Body PlayerConfirmation request
    ) {
        var gameManager = gameAccessor.gameManager(gameId);

        switch (request.confirmationType()) {
            case PlayerConfirmation.PLAY_CONFIRMATION -> gameManager.confirmPlayer(playerId);
            case PlayerConfirmation.PAYMENT_CONFIRMATION -> gameManager.confirmPayment(playerId);
        }
    }

    @Post("/games/{gameId}/registrations")
    @Status(HttpStatus.ACCEPTED)
    void register(@PathVariable("gameId") UUID gameId, @Header("X-Identity-Id") UUID identityId) {
        gameAccessor.playerManager(gameId).signUpPlayer(identityId);
    }

    @Delete("/games/{gameId}/registrations")
    void unregister(@PathVariable("gameId") UUID gameId, @Header("X-Identity-Id") UUID identityId) {
        gameAccessor.playerManager(gameId).signOutPlayer(identityId);
    }

    @Post("/games/{gameId}/results")
    @Status(HttpStatus.ACCEPTED)
    void finish(@PathVariable("gameId") UUID gameId, @Body GameResult request) {
        gameAccessor.gameManager(gameId).finish(request.homeScore(), request.guestScore());
    }

    @Introspected
    record GameCancellation(String reason) {
    }

    @Introspected
    record GameConfirmation(MoneyAmount fee) {
    }

    @Introspected
    record GameResult(int homeScore, int guestScore) {
    }

    @Introspected
    record GameResponse(
            UUID gameId) {
    }

    @Introspected
    record GameRequest(
            UUID locationId,
            Instant date,
            Duration duration,
            SettlementStrategy settlementStrategy,
            int minPlayers,
            int maxPlayers) {
    }

    @Introspected
    record PlayerConfirmation(String confirmationType) {
        private static final String PLAY_CONFIRMATION = "play";
        private static final String PAYMENT_CONFIRMATION = "payment";
    }
}
