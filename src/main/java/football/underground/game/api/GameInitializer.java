package football.underground.game.api;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public interface GameInitializer {
    void initialize(
            UUID organizerId,
            UUID locationId,
            Instant date,
            Duration duration,
            SettlementStrategy settlementStrategy,
            int minPlayers,
            int maxPlayers);
}
