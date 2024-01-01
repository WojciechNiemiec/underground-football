import football.underground.wallet.WalletServicesFactoryImpl;
import football.underground.wallet.api.WalletServicesFactory;

module football.underground.wallet {
    exports football.underground.wallet.api;
    exports football.underground.wallet.event;
    exports football.underground.wallet.spi;

    provides WalletServicesFactory
            with WalletServicesFactoryImpl;

    requires football.underground.eventsourcing;
}
