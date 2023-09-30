package football.underground.game.event;

import java.util.UUID;

public record TeamsDefined(
        UUID homeTeamId,
        UUID guestTeamId) {
}
