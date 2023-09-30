package football.underground.game.api;

public enum SettlementStrategy {
    AUTOMATIC(false),
    MANUAL(true);

    SettlementStrategy(boolean debtAllowed) {
        this.debtAllowed = debtAllowed;
    }

    private final boolean debtAllowed;

    public boolean isDebtAllowed() {
        return debtAllowed;
    }
}
