import football.underground.wallet.WalletAccessorFactoryImpl;
import football.underground.wallet.api.WalletAccessorFactory;

module football.underground.wallet {
    exports football.underground.wallet.api;
    exports football.underground.wallet.event;

    provides WalletAccessorFactory
            with WalletAccessorFactoryImpl;

    requires football.underground.eventsourcing;
}
