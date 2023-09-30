package football.underground.wallet.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record MoneyAmount(BigDecimal value, String currency) {
    public MoneyAmount divideBy(int divisor) {
        return new MoneyAmount(value.divide(BigDecimal.valueOf(divisor), RoundingMode.CEILING), currency);
    }
}
