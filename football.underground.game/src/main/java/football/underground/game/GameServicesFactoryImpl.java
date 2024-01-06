package football.underground.game;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.game.api.GameAccessor;
import football.underground.game.api.GameProjection;
import football.underground.game.api.GameServicesFactory;
import football.underground.game.spi.GameInfoRepository;
import football.underground.game.spi.PaymentSagaRepository;
import football.underground.wallet.api.WalletAccessor;

public class GameServicesFactoryImpl implements GameServicesFactory {
    @Override
    public GameAccessor gameAccessor(
            EventRepository<UUID> gameEventRepository,
            PaymentSagaRepository paymentSagaRepository,
            WalletAccessor walletAccessor
    ) {
        var gameAccessor = new GameAccessorImpl(gameEventRepository);
        var paymentService = new PlayerPaymentService(paymentSagaRepository, gameAccessor, walletAccessor);

        gameEventRepository.subscribe(paymentService, new PlayerPaymentServiceConfiguration());

        return gameAccessor;
    }

    @Override
    public GameProjection gameProjection(
            EventRepository<UUID> gameEventRepository,
            GameInfoRepository gameInfoRepository
    ) {
        var projection = new GameProjectionImpl(gameInfoRepository);

        gameEventRepository.subscribe(projection, new GameProjectionConfiguration());

        return projection;
    }
}
