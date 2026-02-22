package me.a8kj.relayra.core.queue.agent;

import lombok.RequiredArgsConstructor;
import me.a8kj.relayra.api.Relayra;
import me.a8kj.relayra.api.queue.QueueSubscription;
import me.a8kj.relayra.api.queue.RelayQueue;
import me.a8kj.relayra.api.queue.annotation.QueueListener;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

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
                subscribe(listener, method);
            }
        }
    }

    private <T> void subscribe(Object instance, Method method) {
        QueueListener meta = method.getAnnotation(QueueListener.class);
        Class<?> payloadType = method.getParameterTypes()[0];
        RelayQueue<T> queue = relayra.getQueue(meta.value(), (Class<T>) payloadType);

        try {
            new ByteBuddy()
                    .subclass(instance.getClass())
                    .method(ElementMatchers.is(method))
                    .intercept(MethodDelegation.to(new QueueInterceptor(payload -> {
                        try {
                            method.setAccessible(true);
                            method.invoke(instance, payload);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })))
                    .make()
                    .load(instance.getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION);

            queue.process(payload -> {
                try {
                    method.setAccessible(true);
                    method.invoke(instance, payload);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            subscriptions.put(instance, new QueueSubscription() {
                @Override
                public String getQueueName() { return meta.value(); }
                @Override
                public void close() { queue.stop(); }
            });

        } catch (Exception e) {
            throw new RuntimeException("ByteBuddy binding failed", e);
        }
    }

    @Override
    public void close() {
        subscriptions.values().forEach(QueueSubscription::close);
        subscriptions.clear();
    }
}