package me.a8kj.relayra.core.distribution;

import me.a8kj.relayra.api.distribution.ChannelMediator;
import me.a8kj.relayra.api.distribution.RemoteChannel;
import me.a8kj.relayra.codec.CodecAdapter;
import me.a8kj.relayra.codec.impl.JsonCodecFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public final class RelayMediator implements ChannelMediator {

    private final Map<String, Consumer<String>> handlers = new ConcurrentHashMap<>();
    private final Map<String, CodecAdapter<?, String>> adapters = new ConcurrentHashMap<>();


    @Override
    @SuppressWarnings("unchecked")
    public <T> void bind(RemoteChannel<T> channel, Consumer<T> handler) {
        adapters.computeIfAbsent(channel.name(), k -> JsonCodecFactory.create(channel.type()));

        handlers.merge(channel.name(), rawData -> {
            CodecAdapter<T, String> adapter = (CodecAdapter<T, String>) adapters.get(channel.name());
            T payload = adapter.deserialize(rawData);

            if (payload != null) {
                handler.accept(payload);
            }
        }, Consumer::andThen);
    }


    @Override
    public void route(String identifier, String payload) {
        Consumer<String> consumer = handlers.get(identifier);
        if (consumer != null) {
            consumer.accept(payload);
        }
    }
}
