package football.underground.app.api;

import java.util.UUID;

import football.underground.wallet.api.MoneyAmount;
import football.underground.wallet.api.WalletAccessor;
import football.underground.wallet.api.WalletProjection;
import football.underground.wallet.api.WalletProjection.WalletInfo;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;

@Controller("/wallet-api/v1-beta")
class WalletController {
    private final WalletAccessor walletAccessor;
    private final WalletProjection walletProjection;

    public WalletController(WalletAccessor walletAccessor, WalletProjection walletProjection) {
        this.walletAccessor = walletAccessor;
        this.walletProjection = walletProjection;
    }

    @Post("/players/{playerId}/deposits")
    void deposit(
            @PathVariable("playerId") UUID playerId,
            @Header("X-Identity-Id") UUID identityId,
            @Body DepositRequest request
    ) {
        walletAccessor.moneyRegistrar(playerId).register(identityId, request.amount());
    }

    @Get("/my-wallet")
    WalletInfo summary(@Header("X-Identity-Id") UUID identityId) {
        return walletProjection.getWallet(identityId);
    }

    record DepositRequest(MoneyAmount amount) {
    }
}
