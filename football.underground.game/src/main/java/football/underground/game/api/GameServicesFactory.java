package football.underground.game.api;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.game.spi.GameInfoRepository;
import football.underground.game.spi.PaymentSagaRepository;
import football.underground.wallet.api.WalletAccessor;

public interface GameServicesFactory {
    GameAccessor gameAccessor(
            EventRepository<UUID> gameEventRepository,
            PaymentSagaRepository paymentSagaRepository,
            WalletAccessor walletAccessor
    );

    GameProjection gameProjection(
            EventRepository<UUID> gameEventRepository,
            GameInfoRepository gameInfoRepository
    );
}
