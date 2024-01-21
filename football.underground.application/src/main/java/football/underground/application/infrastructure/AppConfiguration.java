package football.underground.application.infrastructure;

import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.Executors;

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
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

@Factory
class AppConfiguration {
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

    @Singleton
    GameAccessor gameAccessor(WalletAccessor walletAccessor) {
        return getWalletServicesFactory(GameServicesFactory.class)
                .gameAccessor(eventRepository, new MongoSagaRepository(database), walletAccessor);
    }

    @Singleton
    GameProjection gameProjection() {
        return getWalletServicesFactory(GameServicesFactory.class)
                .gameProjection(eventRepository, new MongoGameInfoRepository(database));
    }

    @Singleton
    WalletAccessor walletAccessor() {
        return getWalletServicesFactory(WalletServicesFactory.class)
                .walletAccessor(eventRepository);
    }

    @Singleton
    WalletProjection walletProjection() {
        return getWalletServicesFactory(WalletServicesFactory.class)
                .walletProjection(eventRepository, new MongoWalletInfoRepository(database));
    }

    @Singleton
    ObjectMapper objectMapper() {
        return objectMapper;
    }

    @PreDestroy
    void destroy() {
        mongoClient.close();
    }

    private static <T> T getWalletServicesFactory(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow();
    }
}
