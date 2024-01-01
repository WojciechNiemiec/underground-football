package football.underground.game;

import static java.util.UUID.nameUUIDFromBytes;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import football.underground.eventsourcing.Event;
import football.underground.eventsourcing.EventRepository;
import football.underground.game.api.GameAccessor;
import football.underground.game.api.SettlementStrategy;
import football.underground.game.event.GameConfirmed;
import football.underground.game.event.GameInitialized;
import football.underground.game.event.PaymentConfirmed;
import football.underground.game.event.PlayerConfirmed;
import football.underground.game.event.PlayerSignedUp;
import football.underground.game.spi.PaymentSagaRepository;
import football.underground.wallet.api.MoneyAmount;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletServicesFactory;

class PlayerPaymentTest {
    private EventRepository<UUID> eventRepository;
    private WalletAccessor walletAccessor;

    private GameAccessor gameAccessor;

    @BeforeEach
    void setup() {
        eventRepository = EventRepository.inMemory();
        walletAccessor = ServiceLoader.load(WalletServicesFactory.class)
                .findFirst()
                .orElseThrow()
                .walletAccessor(eventRepository);

        gameAccessor = new GameServicesFactoryImpl().gameAccessor(
                eventRepository,
                PaymentSagaRepository.inMemory(),
                walletAccessor
        );
    }

    @Test
    void finish_shouldProceedPaymentForPlayer_givenSufficientAmountInWallet() {
        UUID game = nameUUIDFromBytes("game".getBytes());
        UUID organizer = nameUUIDFromBytes("organizer".getBytes());
        UUID location = nameUUIDFromBytes("location".getBytes());
        UUID player = nameUUIDFromBytes("player".getBytes());

        // given
        walletAccessor.moneyRegistrar(player)
                .register(organizer, new MoneyAmount(BigDecimal.TEN, "USD"));

        var events = Stream.builder()
                .add(new GameInitialized(
                        organizer,
                        location,
                        Instant.EPOCH,
                        Duration.ZERO,
                        SettlementStrategy.MANUAL,
                        1,
                        1
                ))
                .add(new GameConfirmed(new MoneyAmount(BigDecimal.ONE, "USD")))
                .add(new PlayerSignedUp(player))
                .add(new PlayerConfirmed(player))
                .build()
                .map(payload -> new Event<>(Game.class.getCanonicalName(), game, Instant.EPOCH, payload))
                .toList();

        eventRepository.save(events);

        // when
        gameAccessor.gameManager(game).finish(0, 0);

        // then
        assertThat(eventRepository.load(game))
                .last()
                .extracting(Event::payload)
                .isInstanceOf(PaymentConfirmed.class)
                .extracting(PaymentConfirmed.class::cast)
                .returns(player, PaymentConfirmed::playerId);
    }

    @Test
    void finish_shouldNotProceedPaymentForPlayer_givenInsufficientAmountInWallet() {
        UUID game = nameUUIDFromBytes("game".getBytes());
        UUID organizer = nameUUIDFromBytes("organizer".getBytes());
        UUID location = nameUUIDFromBytes("location".getBytes());
        UUID player = nameUUIDFromBytes("player".getBytes());

        // given
        walletAccessor.moneyRegistrar(player)
                .register(organizer, new MoneyAmount(BigDecimal.ONE, "USD"));

        var events = Stream.builder()
                .add(new GameInitialized(
                        organizer,
                        location,
                        Instant.EPOCH,
                        Duration.ZERO,
                        SettlementStrategy.MANUAL,
                        1,
                        1
                ))
                .add(new GameConfirmed(new MoneyAmount(BigDecimal.TEN, "USD")))
                .add(new PlayerSignedUp(player))
                .add(new PlayerConfirmed(player))
                .build()
                .map(payload -> new Event<>(Game.class.getCanonicalName(), game, Instant.EPOCH, payload))
                .toList();

        eventRepository.save(events);

        // when
        gameAccessor.gameManager(game).finish(0, 0);

        // then
        assertThat(eventRepository.load(game))
                .last()
                .extracting(Event::payload)
                .isNotInstanceOf(PaymentConfirmed.class);
    }
}
