import football.underground.game.api.GameServicesFactory;
import football.underground.wallet.api.WalletServicesFactory;

module football.underground.app {
    opens football.underground.app;
    opens football.underground.app.api;
    opens football.underground.app.infrastructure;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires football.underground.eventsourcing;
    requires football.underground.game;
    requires football.underground.wallet;

    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;

    requires org.slf4j;

    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.web;

    uses GameServicesFactory;
    uses WalletServicesFactory;
}
