package me.a8kj.relayra.api;

import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.api.distribution.RelayBus;
import me.a8kj.relayra.api.queue.RelayQueue;

public interface Relayra {

    RelayBus getBus();

    ConnectionSource getConnectionSource();

    void shutdown();

    <T> RelayQueue<T> getQueue(String name, Class<T> type);

    void registerQueueListeners(Object... listeners);

    <T> T createCommandClient(Class<T> clientInterface);

    void subscribe(Object listener);

    <T> T createPublisher(Class<T> publisherInterface);
}