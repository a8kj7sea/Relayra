package me.a8kj.relayra.api.distribution;

import me.a8kj.relayra.util.concurrent.Promise;

import java.util.function.Consumer;

public interface RelayBus {

    <T> Promise<Long> transmit(RemoteChannel<T> channel, T payload);

    <T> Promise<Void> observe(RemoteChannel<T> channel, Consumer<T> handler);
}