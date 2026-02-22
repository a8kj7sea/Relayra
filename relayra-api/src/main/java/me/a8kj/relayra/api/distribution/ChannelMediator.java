package me.a8kj.relayra.api.distribution;

import java.util.function.Consumer;

public interface ChannelMediator {

    <T> void bind(RemoteChannel<T> channel, Consumer<T> handler);

    void route(String identifier, String payload);
}