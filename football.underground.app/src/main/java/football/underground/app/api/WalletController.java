package football.underground.app.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import football.underground.wallet.api.MoneyAmount;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletProjection;
import football.underground.wallet.api.WalletProjection.WalletInfo;

@RestController
@RequestMapping("/wallet-api/v1-beta")
class WalletController {
    private final WalletAccessor walletAccessor;
    private final WalletProjection walletProjection;

    public WalletController(WalletAccessor walletAccessor, WalletProjection walletProjection) {
        this.walletAccessor = walletAccessor;
        this.walletProjection = walletProjection;
    }

    @PostMapping("/players/{playerId}/deposits")
    void deposit(
            @PathVariable("playerId") UUID playerId,
            @RequestHeader("X-Identity-Id") UUID identityId,
            @RequestBody DepositRequest request
    ) {
        walletAccessor.moneyRegistrar(playerId).register(identityId, request.amount());
    }

    @GetMapping("/my-wallet")
    WalletInfo summary(@RequestHeader("X-Identity-Id") UUID identityId) {
        return walletProjection.getWallet(identityId);
    }

    record DepositRequest(MoneyAmount amount) {
    }
}
