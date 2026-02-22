package me.a8kj.relayra.core.messenger.agent;

import lombok.RequiredArgsConstructor;
import me.a8kj.relayra.api.command.annotation.Async;
import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.api.messenger.annotation.RedisPublish;
import me.a8kj.relayra.codec.impl.JsonCodecFactory;
import me.a8kj.relayra.util.concurrent.Promise;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class RelayPublishInterceptor {
    private final ConnectionSource source;

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) {
        RedisPublish annotation = method.getAnnotation(RedisPublish.class);
        if (args.length == 0) return null;

        Object payload = args[0];
        String json = JsonCodecFactory.create((Class<Object>) payload.getClass()).serialize(payload);

        boolean isPromise = method.getReturnType().equals(Promise.class);
        boolean isAsync = method.isAnnotationPresent(Async.class);

        if (isPromise) {
            return Promise.supply(() -> executePublish(annotation.channel(), json));
        }

        if (isAsync) {
            CompletableFuture.runAsync(() -> executePublish(annotation.channel(), json));
            return null;
        }

        return executePublish(annotation.channel(), json);
    }

    private Long executePublish(String channel, String json) {
        try (var conn = source.getPool().borrowObject()) {
            return conn.sync().publish(channel, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}