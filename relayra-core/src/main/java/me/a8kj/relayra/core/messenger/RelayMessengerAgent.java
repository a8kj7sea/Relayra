package me.a8kj.relayra.core.messenger;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import me.a8kj.relayra.api.messenger.annotation.RedisSubscribe;
import me.a8kj.relayra.codec.impl.JsonCodecFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelayMessengerAgent extends RedisPubSubAdapter<String, String> {

    private final Map<String, SubscriptionTask> subscriptions = new ConcurrentHashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(RedisSubscribe.class)) {
                if (method.getParameterCount() != 1) continue;

                RedisSubscribe sub = method.getAnnotation(RedisSubscribe.class);
                method.setAccessible(true);
                subscriptions.put(sub.channel(), new SubscriptionTask(
                        listener,
                        method,
                        method.getParameterTypes()[0]
                ));
            }
        }
    }

    @Override
    public void message(String channel, String message) {
        SubscriptionTask task = subscriptions.get(channel);
        if (task != null) {
            Object payload = JsonCodecFactory.create(task.payloadType).deserialize(message);
            try {
                task.method.invoke(task.instance, payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getSubscribedChannels() {
        return subscriptions.keySet().toArray(new String[0]);
    }

    private record SubscriptionTask(Object instance, Method method, Class<?> payloadType) {}
}