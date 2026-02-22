package me.a8kj.relayra.api.queue;

public interface QueueSubscription extends AutoCloseable {
    String getQueueName();
    @Override
    void close();
}