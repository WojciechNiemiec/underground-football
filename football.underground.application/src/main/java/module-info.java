import football.underground.game.api.GameServicesFactory;
import football.underground.wallet.api.WalletServicesFactory;

module football.underground.application {
    opens football.underground.application;
    opens football.underground.application.api;
    opens football.underground.application.infrastructure;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires football.underground.eventsourcing;
    requires football.underground.game;
    requires football.underground.wallet;

    requires io.micronaut.context;
    requires io.micronaut.http;
    requires io.micronaut.core;
    requires io.micronaut.inject;

    requires jakarta.annotation;
    requires jakarta.inject;

    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;

    requires org.slf4j;

    uses GameServicesFactory;
    uses WalletServicesFactory;
}
