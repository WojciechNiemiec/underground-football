package football.underground.application.infrastructure;

import java.time.Duration;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import football.underground.game.api.GameProjection;

final class MongoClientFactory {
    private MongoClientFactory() {
    }

    static MongoClient create(String mongoUri) {
        CodecRegistry registry = CodecRegistries.fromProviders(
                PojoCodecProvider.builder()
                        .register(GameProjection.GameInfo.class)
                        .build(),
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new DurationCodec())
        );

        return MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(registry)
                .build());
    }

    static class DurationCodec implements Codec<Duration> {
        @Override
        public Duration decode(BsonReader reader, DecoderContext decoderContext) {
            return Duration.parse(reader.readString());
        }

        @Override
        public void encode(BsonWriter writer, Duration value, EncoderContext encoderContext) {
            writer.writeString(value.toString());
        }

        @Override
        public Class<Duration> getEncoderClass() {
            return Duration.class;
        }
    }
}
