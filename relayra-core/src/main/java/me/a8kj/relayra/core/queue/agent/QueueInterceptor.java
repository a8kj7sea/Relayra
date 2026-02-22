package me.a8kj.relayra.core.queue.agent;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class QueueInterceptor {
    private final Consumer<Object> handler;

    @RuntimeType
    public void intercept(@AllArguments Object[] args) {
        if (args != null && args.length > 0) {
            handler.accept(args[0]);
        }
    }
}