package me.a8kj.relayra.core.command.strategy;

import io.lettuce.core.api.sync.RedisCommands;

public interface CommandStrategy {
    Object execute(RedisCommands<String, String> sync, String key, Object[] args);
}
