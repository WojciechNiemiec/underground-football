package football.underground.game;

import football.underground.eventsourcing.EventStream;
import football.underground.game.api.SettlementStrategy;
import football.underground.game.event.*;
import football.underground.wallet.api.MoneyAmount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerSignUpTest {

    private EventStream<UUID, Game> eventStream;
    private Game game;

    @BeforeEach
    void setup() {
        eventStream = new EventStream<>(UUID.nameUUIDFromBytes("a".getBytes()), new AggregateConfiguration());
        game = eventStream.load(Game::new);
    }

    @Test
    void signUp_shouldSign_whenGameConfirmed() {
        UUID organizer = UUID.nameUUIDFromBytes("organizer".getBytes());
        UUID location = UUID.nameUUIDFromBytes("location".getBytes());
        UUID player = UUID.nameUUIDFromBytes("player".getBytes());

        game.handle(new GameInitialized(
                organizer,
                location,
                Instant.EPOCH,
                Duration.ZERO,
                SettlementStrategy.MANUAL,
                10,
                10
        ));
        game.handle(new GameConfirmed(new MoneyAmount(BigDecimal.TEN, "USD")));

        game.signUpPlayer(player);

        assertThat(eventStream.events())
                .singleElement()
                .isEqualTo(new PlayerSignedUp(player));
    }

    @Test
    void confirmation_shouldConfirm_whenPlayerSigned() {
        UUID organizer = UUID.nameUUIDFromBytes("organizer".getBytes());
        UUID location = UUID.nameUUIDFromBytes("location".getBytes());
        UUID player = UUID.nameUUIDFromBytes("player".getBytes());

        game.handle(new GameInitialized(
                organizer,
                location,
                Instant.EPOCH,
                Duration.ZERO,
                SettlementStrategy.MANUAL,
                10,
                10
        ));
        game.handle(new GameConfirmed(new MoneyAmount(BigDecimal.TEN, "USD")));
        game.handle(new PlayerSignedUp(player), Instant.EPOCH);

        game.confirmPlayer(player);

        assertThat(eventStream.events())
                .singleElement()
                .isEqualTo(new PlayerConfirmed(player));
    }

    @Test
    void confirmation_shouldMarkReserve_whenPlayersOverloaded() {
        UUID organizer = UUID.nameUUIDFromBytes("organizer".getBytes());
        UUID location = UUID.nameUUIDFromBytes("location".getBytes());
        UUID player1 = UUID.nameUUIDFromBytes("player1".getBytes());
        UUID player2 = UUID.nameUUIDFromBytes("player2".getBytes());
        UUID player3 = UUID.nameUUIDFromBytes("player3".getBytes());

        game.handle(new GameInitialized(
                organizer,
                location,
                Instant.EPOCH,
                Duration.ZERO,
                SettlementStrategy.MANUAL,
                2,
                2
        ));
        game.handle(new GameConfirmed(new MoneyAmount(BigDecimal.TEN, "USD")));
        game.handle(new PlayerSignedUp(player1), Instant.EPOCH);
        game.handle(new PlayerSignedUp(player2), Instant.EPOCH);
        game.handle(new PlayerSignedUp(player3), Instant.EPOCH);
        game.handle(new PlayerConfirmed(player1));
        game.handle(new PlayerConfirmed(player2));

        game.confirmPlayer(player3);

        assertThat(eventStream.events())
                .singleElement()
                .isEqualTo(new PlayerMarkedReserve(player3));
    }

    @Test
    void signOut_shouldConfirmFirstReservePlayer() {
        UUID organizer = UUID.nameUUIDFromBytes("organizer".getBytes());
        UUID location = UUID.nameUUIDFromBytes("location".getBytes());
        UUID player1 = UUID.nameUUIDFromBytes("player1".getBytes());
        UUID player2 = UUID.nameUUIDFromBytes("player2".getBytes());
        UUID player3 = UUID.nameUUIDFromBytes("player3".getBytes());

        game.handle(new GameInitialized(
                organizer,
                location,
                Instant.EPOCH,
                Duration.ZERO,
                SettlementStrategy.MANUAL,
                1,
                1
        ));
        game.handle(new GameConfirmed(new MoneyAmount(BigDecimal.TEN, "USD")));
        game.handle(new PlayerSignedUp(player1), Instant.EPOCH);
        game.handle(new PlayerSignedUp(player2), Instant.EPOCH.plusSeconds(2));
        game.handle(new PlayerSignedUp(player3), Instant.EPOCH.plusSeconds(3));
        game.handle(new PlayerConfirmed(player1));
        game.handle(new PlayerMarkedReserve(player2));
        game.handle(new PlayerMarkedReserve(player3));

        game.signOutPlayer(player1);

        assertThat(eventStream.events())
                .hasSize(2)
                .containsExactly(new PlayerSignedOut(player1), new PlayerConfirmed(player2));
    }
}
