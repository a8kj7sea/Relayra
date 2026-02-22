package me.a8kj.relayra.config;

import me.a8kj.config.file.BaseConfig;
import me.a8kj.config.file.ConfigFile;
import me.a8kj.config.file.operation.ConfigOperation;
import me.a8kj.config.template.memory.impl.GenericMapDataMemory;
import me.a8kj.config.template.memory.impl.MapPairedDataMemory;
import me.a8kj.relayra.config.util.MapStructureUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;


public class YamlSource extends BaseConfig<String> {

    private final Yaml yaml;


    public YamlSource() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
    }


    @Override
    public ConfigOperation<String> read() {
        return new ConfigOperation<>() {
            @Override
            public void execute(ConfigFile<String> context) throws IOException {
                try (Reader reader = getReader(context.file())) {
                    Map<String, Object> data = yaml.load(reader);
                    if (data != null) {
                        context.memory().clear();
                        MapStructureUtils.flatten("", data, context.memory());
                    }
                } catch (Exception e) {
                    throw new IOException("Critical failure reading YAML source: " + context.file().getName(), e);
                }
            }

            @Override
            public String getName() {
                return "READ";
            }
        };
    }


    @Override
    public ConfigOperation<String> update() {
        return new ConfigOperation<>() {
            @Override
            public void execute(ConfigFile<String> context) throws IOException {
                Map<String, Object> storage = extractStorage(context);

                if (storage == null || storage.isEmpty()) return;

                Map<String, Object> structured = MapStructureUtils.unflatten(storage);

                try (Writer writer = getWriter(context.file())) {
                    yaml.dump(structured, writer);
                    writer.flush();
                } catch (Exception e) {
                    throw new IOException("Critical failure writing YAML source: " + context.file().getName(), e);
                }
            }

            @Override
            public String getName() {
                return "UPDATE";
            }
        };
    }


    private Map<String, Object> extractStorage(ConfigFile<String> context) {
        if (context.memory() instanceof MapPairedDataMemory paired) return paired.getStorage();
        if (context.memory() instanceof GenericMapDataMemory generic) return generic.getStorage();
        return null;
    }
}
