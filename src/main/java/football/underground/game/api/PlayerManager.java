package football.underground.game.api;

import java.util.UUID;

public interface PlayerManager {

    void signUpPlayer(UUID playerId);

    void signOutPlayer(UUID playerId);
}
