package football.underground.game.api;

import java.util.UUID;

import football.underground.wallet.api.MoneyAmount;

public interface GameManager {
    void confirm(MoneyAmount fee);

    void cancel(String reason);

    void finish(int homeScore, int guestScore);

    void defineTeams(UUID homeTeamId, UUID guestTeamId);

    void confirmPlayer(UUID playerId);

    void confirmPayment(UUID playerId);
}
