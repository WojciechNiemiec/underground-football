package football.underground.wallet;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import football.underground.eventsourcing.Appender;
import football.underground.wallet.api.ChargeProxy;
import football.underground.wallet.api.MoneyAmount;
import football.underground.wallet.api.MoneyRegistrar;
import football.underground.wallet.event.ChargeFailed;
import football.underground.wallet.event.ChargeInitiated;
import football.underground.wallet.event.ChargePaid;
import football.underground.wallet.event.MoneyRegistered;

class Wallet implements MoneyRegistrar, ChargeProxy {

    private final Appender stream;

    private final Map<UUID, Pot> potsByCreditor = new HashMap<>();
    private final Map<UUID, Transaction> transactions = new HashMap<>();

    Wallet(Appender stream) {
        this.stream = stream;
    }

    @Override
    public void register(UUID creditorId, MoneyAmount moneyAmount) {
        // TODO: add idempotency or prevent duplicated calls
        stream.append(new MoneyRegistered(creditorId, moneyAmount));
        proceedPendingTransactions();
    }

    @Override
    public void charge(
            UUID transactionId,
            UUID creditorId,
            MoneyAmount moneyAmount,
            boolean debtAllowed
    ) {
        if (transactions.containsKey(transactionId)) {
            throw new IllegalStateException("Transaction already exists");
        }
        stream.append(new ChargeInitiated(transactionId, creditorId, moneyAmount, debtAllowed));
        transactions.get(transactionId).proceed();
    }

    void handle(MoneyRegistered event) {
        Pot pot = potsByCreditor.computeIfAbsent(event.creditorId(), uuid -> new Pot());
        pot.register(event.amount());
    }

    void handle(ChargeInitiated event) {
        transactions.put(
                event.transactionId(),
                new Transaction(event.transactionId(), event.creditorId(), event.amount(), event.debtAllowed())
        );
    }

    void handle(ChargePaid event) {
        Transaction transaction = transactions.get(event.transactionId());

        Pot pot = potsByCreditor.computeIfAbsent(transaction.creditorId, uuid -> new Pot());
        pot.withdraw(transaction.amount);
        transactions.get(event.transactionId()).finish();
    }

    void handle(ChargeFailed event) {
        transactions.get(event.transactionId()).finish();
    }

    private void proceedPendingTransactions() {
        transactions.values().forEach(Transaction::proceed);
    }

    private static class Pot {
        private final Map<String, BigDecimal> amountByCurrency = new HashMap<>();

        void register(MoneyAmount amount) {
            amountByCurrency.compute(amount.currency(), (currency, value) ->
                    (value == null) ? amount.value() : value.add(amount.value())
            );
        }

        void withdraw(MoneyAmount amount) {
            amountByCurrency.compute(amount.currency(), (currency, value) ->
                    (value == null) ? amount.value().negate() : value.subtract(amount.value())
            );
        }

        public boolean canCover(MoneyAmount amount) {
            return amountByCurrency.containsKey(amount.currency()) &&
                   amountByCurrency.get(amount.currency()).compareTo(amount.value()) >= 0;
        }
    }

    private class Transaction {
        private final UUID transactionId;
        private final UUID creditorId;
        private final MoneyAmount amount;
        private final boolean debtAllowed;
        private boolean pending;

        private Transaction(UUID transactionId, UUID creditorId, MoneyAmount amount, boolean debtAllowed) {
            this.transactionId = transactionId;
            this.creditorId = creditorId;
            this.amount = amount;
            this.debtAllowed = debtAllowed;
            pending = true;
        }

        void proceed() {
            if (isPending()) {
                if (potsByCreditor.computeIfAbsent(creditorId, wallet -> new Pot()).canCover(amount)) {
                    stream.append(new ChargePaid(transactionId));
                } else if (!debtAllowed) {
                    stream.append(new ChargeFailed(transactionId));
                }
            }
        }

        void finish() {
            pending = false;
        }

        boolean isPending() {
            return pending;
        }
    }
}
