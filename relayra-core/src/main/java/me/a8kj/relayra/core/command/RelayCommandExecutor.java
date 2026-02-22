package me.a8kj.relayra.core.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.core.command.codec.CommandCodecTransformer;
import me.a8kj.relayra.core.command.strategy.CommandRegistry;
import me.a8kj.relayra.core.command.strategy.CommandStrategy;

import java.lang.reflect.Type;

@RequiredArgsConstructor
public class RelayCommandExecutor {

    private final ConnectionSource source;
    @Getter
    private final CommandRegistry registry = new CommandRegistry();
    private final CommandCodecTransformer transformer = new CommandCodecTransformer();

    public Object dispatch(String action, String key, Object[] args, boolean isAsync, Type returnType) {
        CommandStrategy strategy = registry.get(action);
        if (strategy == null) throw new UnsupportedOperationException("Unsupported: " + action);

        return process(strategy, key, args, returnType);
    }

    private Object process(CommandStrategy strategy, String key, Object[] args, Type type) {
        try (var conn = source.getPool().borrowObject()) {
            Object[] encodedArgs = transformer.encodeArguments(args);
            Object result = strategy.execute(conn.sync(), key, encodedArgs);
            return transformer.decodeResult(result, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}