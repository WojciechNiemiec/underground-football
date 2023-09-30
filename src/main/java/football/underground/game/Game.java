package football.underground.game;

import football.underground.eventsourcing.Appender;
import football.underground.game.api.GameInitializer;
import football.underground.game.api.SettlementStrategy;
import football.underground.game.event.*;
import football.underground.wallet.api.MoneyAmount;
import football.underground.game.api.GameManager;
import football.underground.game.api.PlayerManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import static football.underground.game.Game.State.INITIALIZED;

class Game implements GameManager, PlayerManager {
    private final Appender stream;

    private State state;
    private UUID organizerId;
    private UUID locationId;
    private UUID homeTeamId;
    private UUID guestTeamId;
    private SettlementStrategy settlementStrategy;
    private MoneyAmount fee;
    private int minPlayers;
    private int maxPlayers;
    private final HashMap<UUID, Player> players = new HashMap<>();

    Game(Appender appender) {
        this.stream = appender;
    }

    @Override
    public void confirm(MoneyAmount fee) {
        switch (state) {
            case INITIALIZED -> stream.append(new GameConfirmed(fee));
            case CONFIRMED -> {
            }
            default -> throw new IllegalStateException("Game must be initialized to be confirmed");
        }
    }

    @Override
    public void cancel(String reason) {
        switch (state) {
            case INITIALIZED, CONFIRMED -> stream.append(new GameCancelled(reason));
            case CANCELLED -> {
            }
            default -> throw new IllegalStateException("Game cannot be finished to be cancelled");
        }
    }

    @Override
    public void finish(int homeScore, int guestScore) {
        state.ensureConfirmed();
        stream.append(new GameFinished(homeScore, guestScore));
    }

    @Override
    public void defineTeams(UUID homeTeamId, UUID guestTeamId) {
        state.ensureConfirmed();
        if (!this.homeTeamId.equals(homeTeamId) || !this.guestTeamId.equals(guestTeamId)) {
            stream.append(new TeamsDefined(homeTeamId, guestTeamId));
        }
    }

    @Override
    public void signUpPlayer(UUID playerId) {
        state.ensureConfirmed();
        if (!players.containsKey(playerId)) {
            stream.append(new PlayerSignedUp(playerId));
            if (fee.value().compareTo(BigDecimal.ZERO) > 0) {
                MoneyAmount charge = fee.divideBy(players.size());
                players.get(playerId).initializePayment(charge, settlementStrategy.isDebtAllowed());
            }
        }
    }

    @Override
    public void signOutPlayer(UUID playerId) {
        state.ensureConfirmed();
        if (players.containsKey(playerId)) {
            stream.append(new PlayerSignedOut(playerId));
        }
        players.values()
                .stream()
                .filter(Player::isReserve)
                .sorted(Comparator.comparing(Player::signedAt))
                .findFirst()
                .ifPresent(Player::confirm);
    }

    @Override
    public void confirmPlayer(UUID playerId) {
        state.ensureConfirmed();
        if (!players.containsKey(playerId)) {
            throw new IllegalStateException("Player not found");
        }

        Player player = players.get(playerId);

        if (!player.isConfirmed()) {
            long confirmedPlayers = players.values()
                    .stream()
                    .filter(Player::isConfirmed)
                    .count();

            if (confirmedPlayers < maxPlayers) {
                player.confirm();
            } else {
                player.markReserve();
            }
        }
    }

    @Override
    public void confirmPayment(UUID playerId) {
        state.ensureFinished();
        players.get(playerId).confirmPayment();
    }

    void handle(GameInitialized event) {
        state = INITIALIZED;
        organizerId = event.organizerId();
        locationId = event.locationId();
        settlementStrategy = event.settlementStrategy();
        minPlayers = event.minPlayers();
        maxPlayers = event.maxPlayers();
    }

    void handle(GameConfirmed event) {
        state = State.CONFIRMED;
        fee = event.fee();
    }

    void handleGameCancelled() {
        state = State.CANCELLED;
    }

    void handleGameFinished() {
        state = State.FINISHED;
    }

    void handle(PlayerSignedUp event, Instant eventDate) {
        players.put(event.playerId(), new Player(stream, event.playerId(), eventDate));
    }

    void handle(PlayerSignedOut event) {
        players.remove(event.playerId());
    }

    void handle(PlayerConfirmed event) {
        players.get(event.playerId()).handlePlayerConfirmed();
    }

    void handle(PlayerMarkedReserve event) {
        players.get(event.playerId()).handlePlayerMarkedReserve();
    }

    void handle(TeamsDefined event) {
        homeTeamId = event.homeTeamId();
        guestTeamId = event.guestTeamId();
    }

    void handle(PaymentInitialized event) {
        players.get(event.playerId()).handlePaymentInitialized();
    }

    void handle(PaymentConfirmed event) {
        players.get(event.playerId()).handlePaymentConfirmed();
    }

    private void initialize(
            UUID organizerId,
            UUID locationId,
            Instant date,
            Duration duration,
            SettlementStrategy settlementStrategy,
            int minPlayers,
            int maxPlayers) {
        var init = new GameInitialized(organizerId, locationId, date, duration, settlementStrategy, minPlayers,
                maxPlayers);
        stream.append(init);
    }

    record Initializer(Appender appender) implements GameInitializer {
        @Override
        public void initialize(
                UUID organizerId,
                UUID locationId,
                Instant date,
                Duration duration,
                SettlementStrategy settlementStrategy,
                int minPlayers,
                int maxPlayers) {
            new Game(appender).initialize(organizerId, locationId, date, duration, settlementStrategy, minPlayers, maxPlayers);
        }
    }

    enum State {
        INITIALIZED, CONFIRMED, CANCELLED, FINISHED;

        private void ensureConfirmed() {
            if (this != CONFIRMED) {
                throw new IllegalStateException("Game must be confirmed for this action");
            }
        }

        public void ensureFinished() {
            if (this != FINISHED) {
                throw new IllegalStateException("Game must be finished for this action");
            }
        }
    }
}
