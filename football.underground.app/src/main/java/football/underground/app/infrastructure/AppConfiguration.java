package football.underground.app.infrastructure;

import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import football.underground.eventsourcing.EventRepository;
import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameProjection;
import football.underground.game.api.GameServicesFactory;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletProjection;
import football.underground.wallet.api.WalletServicesFactory;

@Configuration
class AppConfiguration implements DisposableBean {
    private final EventRepository<UUID> eventRepository;
    private final ObjectMapper objectMapper;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    AppConfiguration(@Value("${app.mongo.uri}") String mongoUri) {
        mongoClient = MongoClientFactory.create(mongoUri);
        database = mongoClient.getDatabase("underground-football");
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        eventRepository = new MongoEventRepository<>(
                database,
                Executors.newSingleThreadExecutor(),
                new ObjectSerializer(objectMapper),
                UUID::toString,
                UUID::fromString
        );
    }

    @Bean
    GameAccessor gameAccessor(WalletAccessor walletAccessor) {
        return getWalletServicesFactory(GameServicesFactory.class)
                .gameAccessor(eventRepository, new MongoSagaRepository(database), walletAccessor);
    }

    @Bean
    GameProjection gameProjection() {
        return getWalletServicesFactory(GameServicesFactory.class)
                .gameProjection(eventRepository, new MongoGameInfoRepository(database));
    }

    @Bean
    WalletAccessor walletAccessor() {
        return getWalletServicesFactory(WalletServicesFactory.class)
                .walletAccessor(eventRepository);
    }

    @Bean
    WalletProjection walletProjection() {
        return getWalletServicesFactory(WalletServicesFactory.class)
                .walletProjection(eventRepository, new MongoWalletInfoRepository(database));
    }

    @Bean
    ObjectMapper objectMapper() {
        return objectMapper;
    }

    private static <T> T getWalletServicesFactory(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }
}
