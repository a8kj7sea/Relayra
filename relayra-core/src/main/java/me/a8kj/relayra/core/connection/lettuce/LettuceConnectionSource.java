package me.a8kj.relayra.core.connection.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.Getter;
import me.a8kj.config.template.memory.impl.PairedDataMemory;
import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.config.impl.schema.ConfigSchema;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;


@Getter
public final class LettuceConnectionSource implements ConnectionSource {

    private final RedisClient client;
    private final GenericObjectPool<StatefulRedisConnection<String, String>> pool;
    private final StatefulRedisPubSubConnection<String, String> pubSub;


    public LettuceConnectionSource(PairedDataMemory<String> config) {
        RedisURI uri = RedisURI.builder()
                .withHost(ConfigSchema.HOST.fetch(config))
                .withPort(ConfigSchema.PORT.fetch(config))
                .withPassword(ConfigSchema.PASSWORD.fetch(config).toCharArray())
                .withTimeout(Duration.ofMillis(ConfigSchema.TIMEOUT.fetch(config)))
                .build();

        this.client = RedisClient.create(uri);

        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(ConfigSchema.POOL_MAX_TOTAL.fetch(config));
        poolConfig.setMaxIdle(ConfigSchema.POOL_MAX_IDLE.fetch(config));
        poolConfig.setMinIdle(ConfigSchema.POOL_MIN_IDLE.fetch(config));

        this.pool = ConnectionPoolSupport.createGenericObjectPool(client::connect, poolConfig);
        this.pubSub = client.connectPubSub();
    }


    @Override
    public void close() {
        if (this.pubSub != null) this.pubSub.close();
        if (this.pool != null) this.pool.close();
        if (this.client != null) this.client.shutdown();
    }
}
