package me.a8kj.relayra.api.queue;

import me.a8kj.relayra.util.concurrent.Promise;
import java.util.function.Consumer;

public interface RelayQueue<T> {
    Promise<Long> push(T payload);
    void process(Consumer<T> handler);
    void stop();
}