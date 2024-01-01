package football.underground.wallet;

import java.util.UUID;

import football.underground.eventsourcing.EventRepository;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletProjection;
import football.underground.wallet.api.WalletServicesFactory;
import football.underground.wallet.event.ChargeInitiated;
import football.underground.wallet.event.MoneyRegistered;
import football.underground.wallet.spi.WalletInfoRepository;

public class WalletServicesFactoryImpl implements WalletServicesFactory {
    @Override
    public WalletAccessor walletAccessor(EventRepository<UUID> walletEventRepository) {
        return new WalletAccessorImpl(walletEventRepository);
    }

    @Override
    public WalletProjection walletProjection(
            EventRepository<UUID> walletEventRepository,
            WalletInfoRepository walletInfoRepository
    ) {
        var projection = new WalletProjectionImpl(walletInfoRepository);

        walletEventRepository.subscribe(projection, subscriber -> {
            subscriber.subscribeWithMeta(
                    MoneyRegistered.class,
                    (service, event, id, date) -> projection.handle(event, id)
            );
            subscriber.subscribeWithMeta(
                    ChargeInitiated.class,
                    (service, event, id, date) -> projection.handle(event, id)
            );
        });

        return projection;
    }
}
