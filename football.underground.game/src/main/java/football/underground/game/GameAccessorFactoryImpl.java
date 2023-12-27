package football.underground.game;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameAccessorFactory;
import football.underground.game.spi.SagaRepository;
import football.underground.wallet.api.WalletAccessor;

public class GameAccessorFactoryImpl implements GameAccessorFactory {
    @Override
    public GameAccessor create(
            EventRepository<UUID> gameEventRepository,
            SagaRepository sagaRepository,
            WalletAccessor walletAccessor
    ) {
        GameAccessor gameAccessor = new GameAccessorImpl(gameEventRepository);

        var service = new PlayerPaymentService(sagaRepository, gameAccessor, walletAccessor);
        gameEventRepository.subscribe(service, new PlayerPaymentServiceConfiguration());

        return gameAccessor;
    }
}
