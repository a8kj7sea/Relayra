package me.a8kj.relayra.core.distribution;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.api.distribution.ChannelMediator;
import me.a8kj.relayra.api.distribution.RelayBus;
import me.a8kj.relayra.api.distribution.RemoteChannel;
import me.a8kj.relayra.codec.CodecAdapter;
import me.a8kj.relayra.codec.impl.JsonCodecFactory;
import me.a8kj.relayra.util.concurrent.Promise;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


public final class LettuceRelayBus implements RelayBus {

    private final ConnectionSource source;
    private final ChannelMediator mediator;


    public LettuceRelayBus(ConnectionSource source, ChannelMediator mediator) {
        this.source = source;
        this.mediator = mediator;

        this.source.getPubSub().addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                mediator.route(channel, message);
            }
        });
    }


    @Override
    public <T> Promise<Long> transmit(RemoteChannel<T> channel, T payload) {
        return Promise.of(CompletableFuture.supplyAsync(() -> {
            try (StatefulRedisConnection<String, String> connection = source.getPool().borrowObject()) {
                CodecAdapter<T, String> adapter = JsonCodecFactory.create(channel.type());
                String serialized = adapter.serialize(payload);
                return connection.async().publish(channel.name(), serialized).get();
            } catch (Exception e) {
                throw new RuntimeException("Relay transmission failure on: " + channel.name(), e);
            }
        }));
    }


    @Override
    public <T> Promise<Void> observe(RemoteChannel<T> channel, Consumer<T> handler) {
        this.mediator.bind(channel, handler);

        CompletableFuture<Void> subscription = source.getPubSub().async()
                .subscribe(channel.name())
                .thenAccept(v -> {
                })
                .toCompletableFuture();

        return Promise.of(subscription);
    }
}
