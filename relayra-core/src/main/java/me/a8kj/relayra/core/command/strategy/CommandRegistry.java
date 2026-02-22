package me.a8kj.relayra.core.command.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CommandRegistry {
    private final Map<String, CommandStrategy> strategies = new ConcurrentHashMap<>();

    public CommandRegistry() {
        register("GET", (sync, key, args) -> sync.get(key));
        register("SET", (sync, key, args) -> sync.set(key, String.valueOf(args[1])));
        register("DEL", (sync, key, args) -> sync.del(key));
        register("INCR", (sync, key, args) -> sync.incr(key));
        register("EXISTS", (sync, key, args) -> sync.exists(key));
    }

    public void register(String cmd, CommandStrategy s) {
        strategies.put(cmd.toUpperCase(), s);
    }

    public CommandStrategy get(String cmd) {
        return strategies.get(cmd.toUpperCase());
    }
}