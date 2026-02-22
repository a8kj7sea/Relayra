package me.a8kj.relayra.core.queue.agent;

import lombok.RequiredArgsConstructor;
import me.a8kj.logging.Log;
import me.a8kj.relayra.api.Relayra;
import me.a8kj.relayra.api.queue.QueueSubscription;
import me.a8kj.relayra.api.queue.RelayQueue;
import me.a8kj.relayra.api.queue.annotation.QueueListener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class RelayraQueueAgent implements AutoCloseable {

    private final Relayra relayra;
    private final Map<Object, QueueSubscription> subscriptions = new ConcurrentHashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(QueueListener.class)) {
                if (method.getParameterCount() != 1) continue;
                subscribe(listener, method);
            }
        }
    }

    private <T> void subscribe(Object instance, Method method) {
        QueueListener meta = method.getAnnotation(QueueListener.class);
        Class<?> payloadType = method.getParameterTypes()[0];
        RelayQueue<T> queue = relayra.getQueue(meta.value(), (Class<T>) payloadType);

        method.setAccessible(true);

        queue.process(payload -> {
            try {
                method.invoke(instance, payload);
            } catch (Exception e) {
                Log.error("Failed to invoke queue handler for " + meta.value(), e);
                e.printStackTrace();
            }
        });

        subscriptions.put(instance, new QueueSubscription() {
            @Override
            public String getQueueName() {
                return meta.value();
            }

            @Override
            public void close() {
                queue.stop();
            }
        });
    }

    @Override
    public void close() {
        subscriptions.values().forEach(QueueSubscription::close);
        subscriptions.clear();
    }
}