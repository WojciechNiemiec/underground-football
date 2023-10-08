package football.underground.game.event;

import football.underground.game.api.SettlementStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record GameInitialized(
        UUID organizerId,
        UUID locationId,
        Instant date,
        Duration duration,
        SettlementStrategy settlementStrategy,
        int minPlayers,
        int maxPlayers) {
}
