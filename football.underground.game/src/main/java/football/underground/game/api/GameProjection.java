package football.underground.game.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import football.underground.wallet.api.MoneyAmount;

public interface GameProjection {
    GameInfo getGame(UUID gameId);

    GamePage getGames(int page, int pageSize, String state, UUID locationId, UUID organizerId);

    record GamePage(
            int page,
            int pageSize,
            long totalPages,
            long totalElements,
            List<GameInfo> content) {
    }

    class GameInfo {
        private UUID gameId;
        private UUID organizerId;
        private UUID locationId;
        private Instant date;
        private Duration duration;
        private SettlementStrategy settlementStrategy;
        private int minPlayers;
        private int maxPlayers;
        private Map<String, PlayerInfo> players;
        private String state;
        private MoneyAmount fee;
        private UUID homeTeamId;
        private UUID guestTeamId;

        public UUID getGameId() {
            return gameId;
        }

        public void setGameId(UUID gameId) {
            this.gameId = gameId;
        }

        public UUID getOrganizerId() {
            return organizerId;
        }

        public void setOrganizerId(UUID organizerId) {
            this.organizerId = organizerId;
        }

        public UUID getLocationId() {
            return locationId;
        }

        public void setLocationId(UUID locationId) {
            this.locationId = locationId;
        }

        public Instant getDate() {
            return date;
        }

        public void setDate(Instant date) {
            this.date = date;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public SettlementStrategy getSettlementStrategy() {
            return settlementStrategy;
        }

        public void setSettlementStrategy(SettlementStrategy settlementStrategy) {
            this.settlementStrategy = settlementStrategy;
        }

        public int getMinPlayers() {
            return minPlayers;
        }

        public void setMinPlayers(int minPlayers) {
            this.minPlayers = minPlayers;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        public String getState() {
            return state;
        }

        public Map<String, PlayerInfo> getPlayers() {
            return players;
        }

        public void setPlayers(Map<String, PlayerInfo> players) {
            this.players = players;
        }

        public void setState(String state) {
            this.state = state;
        }

        public MoneyAmount getFee() {
            return fee;
        }

        public void setFee(MoneyAmount fee) {
            this.fee = fee;
        }

        public UUID getHomeTeamId() {
            return homeTeamId;
        }

        public void setHomeTeamId(UUID homeTeamId) {
            this.homeTeamId = homeTeamId;
        }

        public UUID getGuestTeamId() {
            return guestTeamId;
        }

        public void setGuestTeamId(UUID guestTeamId) {
            this.guestTeamId = guestTeamId;
        }
    }

    record PlayerInfo(
            String id,
            PlayStatus playStatus,
            PaymentStatus paymentStatus) {
    }

    enum PlayStatus {
        PENDING, CONFIRMED, RESERVE
    }

    enum PaymentStatus {
        UNPAID, INITIALIZED, COMPLETED
    }
}
