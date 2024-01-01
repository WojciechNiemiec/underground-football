package football.underground.game.api;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
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
        private final UUID id;
        private final UUID organizerId;
        private final UUID locationId;
        private final Instant date;
        private final Duration duration;
        private final SettlementStrategy settlementStrategy;
        private final int minPlayers;
        private final int maxPlayers;
        private final Map<UUID, PlayerInfo> players;
        private String state;
        private MoneyAmount fee;
        private UUID homeTeamId;
        private UUID guestTeamId;

        public GameInfo(
                UUID id,
                UUID organizerId,
                UUID locationId,
                Instant date,
                Duration duration,
                SettlementStrategy settlementStrategy,
                int minPlayers,
                int maxPlayers
        ) {
            this.id = id;
            this.organizerId = organizerId;
            this.locationId = locationId;
            this.date = date;
            this.duration = duration;
            this.settlementStrategy = settlementStrategy;
            this.minPlayers = minPlayers;
            this.maxPlayers = maxPlayers;
            this.players = new HashMap<>();
            this.state = "INITIALIZED";
        }

        public UUID getId() {
            return id;
        }

        public UUID getOrganizerId() {
            return organizerId;
        }

        public UUID getLocationId() {
            return locationId;
        }

        public Instant getDate() {
            return date;
        }

        public Duration getDuration() {
            return duration;
        }

        public SettlementStrategy getSettlementStrategy() {
            return settlementStrategy;
        }

        public int getMinPlayers() {
            return minPlayers;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public Map<UUID, PlayerInfo> getPlayers() {
            return players;
        }

        public String getState() {
            return state;
        }

        public MoneyAmount getFee() {
            return fee;
        }

        public UUID getHomeTeamId() {
            return homeTeamId;
        }

        public UUID getGuestTeamId() {
            return guestTeamId;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setFee(MoneyAmount fee) {
            this.fee = fee;
        }

        public void setHomeTeamId(UUID homeTeamId) {
            this.homeTeamId = homeTeamId;
        }

        public void setGuestTeamId(UUID guestTeamId) {
            this.guestTeamId = guestTeamId;
        }
    }

    record PlayerInfo(
            UUID id,
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
