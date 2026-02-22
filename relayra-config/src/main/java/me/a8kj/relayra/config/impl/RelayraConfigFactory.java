package me.a8kj.relayra.config.impl;

import lombok.experimental.UtilityClass;
import me.a8kj.config.ConfigProvider;
import me.a8kj.config.builder.BasicConfigBuilder;
import me.a8kj.config.context.ConfigExecutionContext;
import me.a8kj.config.context.impl.BasicConfigExecutionContext;
import me.a8kj.config.file.PathProviders;
import me.a8kj.config.file.operation.impl.CRUDOperations;
import me.a8kj.config.file.properties.BasicConfigMeta;
import me.a8kj.config.template.memory.impl.MapPairedDataMemory;
import me.a8kj.config.template.memory.impl.PairedDataMemory;

import java.io.File;

@UtilityClass
public class RelayraConfigFactory {

    @SuppressWarnings("unchecked")
    public static PairedDataMemory<String> create(String name, String path) {
        try {
            var config = BasicConfigBuilder.of(String.class)
                    .meta(BasicConfigMeta.builder()
                            .name(name)
                            .relativePath(name + ".yml")
                            .build())
                    .at(PathProviders.custom(path))
                    .memory(new MapPairedDataMemory())
                    .build();

            ConfigProvider.provide().getConfigRegistry().register(name, config);
            ConfigExecutionContext<String> context =
                    BasicConfigExecutionContext.of(name, new RelayraConfig());

            File file = new File(path, name + ".yml");
            boolean exists = file.exists() && file.length() > 0;

            context.execute(CRUDOperations::create);

            if (exists) {
                context.execute(CRUDOperations::read);
            }

            PairedDataMemory<String> memory =
                    (PairedDataMemory<String>) context.config().memory();

            if (!exists || memory.get("connection.host") == null) {
                injectDefaults(memory);
                context.execute(CRUDOperations::update);
            }

            return memory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + name, e);
        }
    }

    private static void injectDefaults(PairedDataMemory<String> memory) {
        memory.set("connection.host", "127.0.0.1");
        memory.set("connection.port", 6379);
        memory.set("connection.password", "");
        memory.set("connection.timeout", 2000);
        memory.set("pool.max-total", 8);
        memory.set("pool.max-idle", 8);
        memory.set("pool.min-idle", 0);
        memory.set("logger.enabled", true);
    }

    public static void update(String name) {
        try {
            BasicConfigExecutionContext.of(name, new RelayraConfig())
                    .execute(CRUDOperations::update);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}