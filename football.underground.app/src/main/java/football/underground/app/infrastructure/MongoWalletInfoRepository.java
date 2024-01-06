package football.underground.app.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import football.underground.wallet.api.WalletProjection.WalletInfo;
import football.underground.wallet.spi.WalletInfoRepository;

class MongoWalletInfoRepository implements WalletInfoRepository {
    private static final String COLLECTION = "wallets";

    private final MongoCollection<WalletInfo> walletCollection;

    public MongoWalletInfoRepository(MongoDatabase database) {
        this.walletCollection = database.getCollection(COLLECTION, WalletInfo.class);
    }

    @Override
    public Optional<WalletInfo> getWallet(UUID accountId) {
        return Optional.ofNullable(walletCollection.find(accountIdEquals(accountId)).first());
    }

    @Override
    public void save(WalletInfo walletInfo) {
        walletCollection.replaceOne(
                accountIdEquals(walletInfo.accountId()),
                walletInfo,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void delete(UUID accountId) {
        walletCollection.deleteOne(accountIdEquals(accountId));
    }

    private static BasicDBObject accountIdEquals(UUID accountId) {
        return new BasicDBObject("accountId", accountId);
    }
}
