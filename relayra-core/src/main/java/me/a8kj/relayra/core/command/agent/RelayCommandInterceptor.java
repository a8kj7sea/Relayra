package me.a8kj.relayra.core.command.agent;

import me.a8kj.relayra.api.command.annotation.Async;
import me.a8kj.relayra.api.command.annotation.RelayCommand;
import me.a8kj.relayra.core.command.RelayCommandExecutor;
import me.a8kj.relayra.core.command.util.CommandKeyParser;
import me.a8kj.relayra.util.concurrent.Promise;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RelayCommandInterceptor {
    private final RelayCommandExecutor executor;

    public RelayCommandInterceptor(RelayCommandExecutor executor) {
        this.executor = executor;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) {
        RelayCommand cmd = method.getAnnotation(RelayCommand.class);
        String key = CommandKeyParser.parse(cmd.key(), args);

        Type returnType = method.getGenericReturnType();
        boolean isPromise = method.getReturnType().equals(Promise.class);

        if (isPromise && returnType instanceof ParameterizedType) {
            returnType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        }

        boolean isAsync = method.isAnnotationPresent(Async.class) || isPromise;

        final Type finalReturnType = returnType;
        if (isPromise) {
            return Promise.supply(() -> executor.dispatch(cmd.command(), key, args, false, finalReturnType));
        }

        return executor.dispatch(cmd.command(), key, args, isAsync, returnType);
    }
}