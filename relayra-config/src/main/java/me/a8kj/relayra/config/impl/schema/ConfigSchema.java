package me.a8kj.relayra.config.impl.schema;

import me.a8kj.config.template.memory.MemoryDataType;
import me.a8kj.config.template.memory.MemoryEntry;

public final class ConfigSchema {

    private ConfigSchema() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final MemoryEntry<String> HOST =
            MemoryEntry.of("connection.host", MemoryDataType.STRING);

    public static final MemoryEntry<Integer> PORT =
            MemoryEntry.of("connection.port", MemoryDataType.INTEGER);

    public static final MemoryEntry<String> PASSWORD =
            MemoryEntry.of("connection.password", MemoryDataType.STRING);

    public static final MemoryEntry<Integer> TIMEOUT =
            MemoryEntry.of("connection.timeout", MemoryDataType.INTEGER);

    public static final MemoryEntry<Integer> POOL_MAX_TOTAL =
            MemoryEntry.of("pool.max-total", MemoryDataType.INTEGER);

    public static final MemoryEntry<Integer> POOL_MAX_IDLE =
            MemoryEntry.of("pool.max-idle", MemoryDataType.INTEGER);

    public static final MemoryEntry<Integer> POOL_MIN_IDLE =
            MemoryEntry.of("pool.min-idle", MemoryDataType.INTEGER);

    public static final MemoryEntry<Boolean> LOGGER_ENABLED =
            MemoryEntry.of("logger.enabled", MemoryDataType.BOOLEAN);
}