package me.a8kj.relayra.config;

import lombok.NoArgsConstructor;
import me.a8kj.config.file.ConfigFile;
import me.a8kj.logging.Log;


@NoArgsConstructor
public class AutoYamlSource extends YamlSource {


    @Override
    protected void onPostCreate(ConfigFile<String> config) {
        super.onPostCreate(config);

        try {
            this.read().execute(config);
        } catch (Exception e) {
            Log.error("Failed to auto-read YAML source '%s' after creation: %s",
                    config.meta().getName(), e.getMessage());
        }
    }
}
