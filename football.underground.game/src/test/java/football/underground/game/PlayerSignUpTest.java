package football.underground.game;

import static java.util.UUID.nameUUIDFromBytes;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import football.underground.eventsourcing.EventStream;
import football.underground.game.api.SettlementStrategy;
import football.underground.game.event.GameConfirmed;
import football.underground.game.event.GameInitialized;
import football.underground.game.event.PlayerConfirmed;
import football.underground.game.event.PlayerMarkedReserve;
import football.underground.game.event.PlayerSignedOut;
import football.underground.game.event.PlayerSignedUp;
import football.underground.wallet.api.MoneyAmount;

class PlayerSignUpTest {
    private EventStream<Game, UUID> eventStream;
    private Game game;

    @BeforeEach
    void setup() {
        eventStream = new EventStream<>(nameUUIDFromBytes("a".getBytes()), new AggregateConfiguration(), Game::new);
        game = eventStream.load();
    }

    @Test
    void signUp_shouldSign_whenGameConfirmed() {
        UUID organizer = nameUUIDFromBytes("organizer".getBytes());
        UUID location = nameUUIDFromBytes("location".getBytes());
        UUID player = nameUUIDFromBytes("player".getBytes());

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
        UUID organizer = nameUUIDFromBytes("organizer".getBytes());
        UUID location = nameUUIDFromBytes("location".getBytes());
        UUID player = nameUUIDFromBytes("player".getBytes());

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
        UUID organizer = nameUUIDFromBytes("organizer".getBytes());
        UUID location = nameUUIDFromBytes("location".getBytes());
        UUID player1 = nameUUIDFromBytes("player1".getBytes());
        UUID player2 = nameUUIDFromBytes("player2".getBytes());
        UUID player3 = nameUUIDFromBytes("player3".getBytes());

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
        UUID organizer = nameUUIDFromBytes("organizer".getBytes());
        UUID location = nameUUIDFromBytes("location".getBytes());
        UUID player1 = nameUUIDFromBytes("player1".getBytes());
        UUID player2 = nameUUIDFromBytes("player2".getBytes());
        UUID player3 = nameUUIDFromBytes("player3".getBytes());

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
