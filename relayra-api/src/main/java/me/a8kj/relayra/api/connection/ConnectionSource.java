package me.a8kj.relayra.api.connection;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;

public interface ConnectionSource extends AutoCloseable {

    GenericObjectPool<StatefulRedisConnection<String, String>> getPool();

    StatefulRedisPubSubConnection<String, String> getPubSub();
}