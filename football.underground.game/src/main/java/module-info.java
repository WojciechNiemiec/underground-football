import football.underground.game.GameServicesFactoryImpl;
import football.underground.game.api.GameServicesFactory;
import football.underground.wallet.api.WalletServicesFactory;

module football.underground.game {
    exports football.underground.game.api;
    exports football.underground.game.spi;

    opens football.underground.game.api;
    opens football.underground.game.event;

    provides GameServicesFactory
            with GameServicesFactoryImpl;

    requires football.underground.eventsourcing;
    requires football.underground.wallet;

    uses WalletServicesFactory;
}
