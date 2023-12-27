import football.underground.wallet.api.WalletAccessorFactory;

module football.underground.game {
    exports football.underground.game.api;
    exports football.underground.game.spi;

    provides football.underground.game.api.GameAccessorFactory
            with football.underground.game.GameAccessorFactoryImpl;

    requires football.underground.eventsourcing;
    requires football.underground.wallet;

    uses WalletAccessorFactory;
}
