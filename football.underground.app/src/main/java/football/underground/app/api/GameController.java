package football.underground.app.api;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameProjection;
import football.underground.game.api.GameProjection.GamePage;
import football.underground.game.api.SettlementStrategy;
import football.underground.wallet.api.MoneyAmount;

@RestController
@RequestMapping("/game-api/v1-beta")
class GameController {
    private final GameAccessor gameAccessor;
    private final GameProjection gameProjection;

    public GameController(GameAccessor gameAccessor, GameProjection gameProjection) {
        this.gameAccessor = gameAccessor;
        this.gameProjection = gameProjection;
    }

    @GetMapping("/games")
    GamePage lookup() {
        return gameProjection.getGames(0, 100, null, null, null);
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    GameResponse create(@RequestBody GameRequest request, @RequestHeader("X-Identity-Id") UUID identityId) {
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

    @PostMapping("/games/{gameId}/cancellations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void cancel(@PathVariable("gameId") UUID gameId, @RequestBody GameCancellation request) {
        gameAccessor.gameManager(gameId).cancel(request.reason());
    }

    @PostMapping("/games/{gameId}/confirmations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void confirm(@PathVariable("gameId") UUID gameId, @RequestBody GameConfirmation confirmation) {
        gameAccessor.gameManager(gameId).confirm(confirmation.fee());
    }

    @PostMapping("/games/{gameId}/players/{playerId}/confirmations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void confirmPlayer(
            @PathVariable("gameId") UUID gameId,
            @PathVariable("playerId") UUID playerId,
            @RequestBody PlayerConfirmation request
    ) {
        var gameManager = gameAccessor.gameManager(gameId);

        switch (request.confirmationType()) {
            case PlayerConfirmation.PLAY_CONFIRMATION -> gameManager.confirmPlayer(playerId);
            case PlayerConfirmation.PAYMENT_CONFIRMATION -> gameManager.confirmPayment(playerId);
        }
    }

    @PostMapping("/games/{gameId}/registrations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void register(@PathVariable("gameId") UUID gameId, @RequestHeader("X-Identity-Id") UUID identityId) {
        gameAccessor.playerManager(gameId).signUpPlayer(identityId);
    }

    @DeleteMapping("/games/{gameId}/registrations")
    void unregister(@PathVariable("gameId") UUID gameId, @RequestHeader("X-Identity-Id") UUID identityId) {
        gameAccessor.playerManager(gameId).signOutPlayer(identityId);
    }

    @PostMapping("/games/{gameId}/results")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void finish(@PathVariable("gameId") UUID gameId, @RequestBody GameResult request) {
        gameAccessor.gameManager(gameId).finish(request.homeScore(), request.guestScore());
    }

    record GameCancellation(String reason) {
    }

    record GameConfirmation(MoneyAmount fee) {
    }

    record GameResult(int homeScore, int guestScore) {
    }

    record GameResponse(
            UUID gameId) {
    }

    record GameRequest(
            UUID locationId,
            Instant date,
            Duration duration,
            SettlementStrategy settlementStrategy,
            int minPlayers,
            int maxPlayers) {
    }

    record PlayerConfirmation(String confirmationType) {
        private static final String PLAY_CONFIRMATION = "play";
        private static final String PAYMENT_CONFIRMATION = "payment";
    }
}
