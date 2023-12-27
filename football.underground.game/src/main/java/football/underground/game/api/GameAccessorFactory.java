package football.underground.game.api;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.game.spi.SagaRepository;
import football.underground.wallet.api.WalletAccessor;

public interface GameAccessorFactory {
    GameAccessor create(
            EventRepository<UUID> gameEventRepository,
            SagaRepository sagaRepository,
            WalletAccessor walletAccessor
    );
}
