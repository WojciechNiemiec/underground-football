package football.underground.app.infrastructure;

import java.util.ServiceLoader;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import football.underground.eventsourcing.EventRepository;
import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameProjection;
import football.underground.game.api.GameServicesFactory;
import football.underground.game.spi.GameProjectionRepository;
import football.underground.game.spi.PaymentSagaRepository;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletProjection;
import football.underground.wallet.api.WalletServicesFactory;
import football.underground.wallet.spi.WalletInfoRepository;

@Configuration
class AppConfiguration {
    private final EventRepository<UUID> eventRepository;

    AppConfiguration() {
        eventRepository = EventRepository.inMemory();
    }

    @Bean
    GameAccessor gameAccessor(WalletAccessor walletAccessor) {
        return getWalletServicesFactory(GameServicesFactory.class)
                .gameAccessor(eventRepository, PaymentSagaRepository.inMemory(), walletAccessor);
    }

    @Bean
    GameProjection gameProjection() {
        return getWalletServicesFactory(GameServicesFactory.class)
                .gameProjection(eventRepository, GameProjectionRepository.inMemory());
    }

    @Bean
    WalletAccessor walletAccessor() {
        return getWalletServicesFactory(WalletServicesFactory.class)
                .walletAccessor(eventRepository);
    }

    @Bean
    WalletProjection walletProjection() {
        return getWalletServicesFactory(WalletServicesFactory.class)
                .walletProjection(eventRepository, WalletInfoRepository.inMemory());
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    private static <T> T getWalletServicesFactory(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow();
    }
}
