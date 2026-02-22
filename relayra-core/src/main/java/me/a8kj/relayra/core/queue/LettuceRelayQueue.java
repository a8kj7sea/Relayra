package me.a8kj.relayra.core.queue;

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
                return conn.async().lpush(name, adapter.serialize(payload)).get();
            }
        });
    }


    @Override
    public void process(Consumer<T> handler) {
        if (running.getAndSet(true)) return;

        Thread worker = new Thread(() -> {
            CodecAdapter<T, String> adapter = JsonCodecFactory.create(type);
            try (var conn = source.getPool().borrowObject()) {
                while (running.get() && !Thread.currentThread().isInterrupted()) {
                    try {
                        var result = conn.async().brpop(5, name).get();
                        if (result != null) {
                            T data = adapter.deserialize(result.getValue());
                            if (data != null) handler.accept(data);
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Relayra-Worker-" + name);

        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void stop() {
        running.set(false);
    }
}