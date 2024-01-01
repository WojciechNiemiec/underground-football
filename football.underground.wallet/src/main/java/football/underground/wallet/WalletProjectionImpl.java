package football.underground.wallet;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import football.underground.wallet.api.MoneyAmount;
import football.underground.wallet.api.WalletProjection;
import football.underground.wallet.event.ChargeInitiated;
import football.underground.wallet.event.MoneyRegistered;
import football.underground.wallet.spi.WalletInfoRepository;

class WalletProjectionImpl implements WalletProjection {
    private final WalletInfoRepository repository;

    @Override
    public WalletInfo getWallet(UUID accountId) {
        return repository.getWallet(accountId).orElseThrow();
    }

    WalletProjectionImpl(WalletInfoRepository repository) {
        this.repository = repository;
    }

    void handle(MoneyRegistered event, UUID accountId) {
        alterBalance(accountId, event.creditorId(), event.amount());
    }

    void handle(ChargeInitiated event, UUID accountId) {
        MoneyAmount amount = new MoneyAmount(
                event.amount().value().negate(),
                event.amount().currency()
        );
        alterBalance(accountId, event.creditorId(), amount);
    }

    // enforce idempotency
    private void alterBalance(UUID accountId, UUID creditorId, MoneyAmount deltaAmount) {
        WalletInfo walletInfo = repository.getWallet(accountId).orElseGet(() -> new WalletInfo(
                accountId,
                new ArrayList<>()
        ));
        Optional<Balance> existingBalance = walletInfo.balances()
                .stream()
                .filter(existing -> existing.creditorId().equals(creditorId) &&
                                    existing.amount().currency().equals(deltaAmount.currency()))
                .findFirst();

        existingBalance.ifPresent(walletInfo.balances()::remove);

        Balance newBalance = existingBalance
                .map(existing -> new Balance(
                                existing.creditorId(),
                                new MoneyAmount(
                                        existing.amount().value().add(deltaAmount.value()),
                                        existing.amount().currency()
                                )
                        )
                )
                .orElseGet(() -> new Balance(creditorId, deltaAmount));

        walletInfo.balances().add(newBalance);

        repository.save(walletInfo);
    }
}
