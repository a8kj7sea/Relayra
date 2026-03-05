package me.a8kj.relayra.core.queue;

import io.lettuce.core.KeyValue;
import io.lettuce.core.api.StatefulRedisConnection;
import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.api.queue.RelayQueue;
import me.a8kj.relayra.codec.CodecAdapter;
import me.a8kj.relayra.codec.impl.JsonCodecFactory;
import me.a8kj.relayra.util.concurrent.AsyncService;
import me.a8kj.relayra.util.concurrent.Promise;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LettuceRelayQueue<T> extends AsyncService implements RelayQueue<T> {

    private final String name;
    private final Class<T> type;
    private final ConnectionSource source;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;

    public LettuceRelayQueue(String name, Class<T> type, ConnectionSource source) {
        this.name = name;
        this.type = type;
        this.source = source;
    }

    @Override
    public Promise<Long> push(T payload) {
        CodecAdapter<T, String> adapter = JsonCodecFactory.create(type);
        return async(() -> {
            try (StatefulRedisConnection<String, String> conn = source.getPool().borrowObject()) {
                return conn.sync().lpush(name, adapter.serialize(payload));
            }
        });
    }

    @Override
    public void process(Consumer<T> handler) {
        if (running.getAndSet(true)) return;

        worker = new Thread(() -> {
            CodecAdapter<T, String> adapter = JsonCodecFactory.create(type);
            int failureStreak = 0;

            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try (StatefulRedisConnection<String, String> conn = source.getPool().borrowObject()) {
                    while (running.get() && !Thread.currentThread().isInterrupted()) {
                        KeyValue<String, String> result = conn.sync().brpop(5, name);

                        if (result != null) {
                            failureStreak = 0;
                            T data = adapter.deserialize(result.getValue());
                            if (data != null) handler.accept(data);
                        }
                    }
                } catch (Exception e) {
                    failureStreak++;
                    long backoff = (long) Math.min(30000.0, Math.pow(2, failureStreak) * 1000);

                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Relayra-Worker-" + name);

        worker.setDaemon(false);
        worker.start();
    }

    @Override
    public void stop() {
        running.set(false);
        if (worker != null) {
            worker.interrupt();
        }
    }
}