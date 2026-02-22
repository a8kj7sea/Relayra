package me.a8kj.relayra.config.util;

import me.a8kj.config.template.memory.DataMemory;
import lombok.experimental.UtilityClass;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public final class MapStructureUtils {

    public static void flatten(String prefix, Map<String, Object> source, DataMemory<String> memory) {
        if (source == null) return;

        source.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map<?, ?> subMap) {
                flatten(fullKey, (Map<String, Object>) subMap, memory);
            } else {
                memory.set(fullKey, value);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> unflatten(Map<String, Object> flatMap) {
        if (flatMap == null) return new LinkedHashMap<>();

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String[] keys = entry.getKey().split("\\.");
            Map<String, Object> current = result;

            for (int i = 0; i < keys.length - 1; i++) {
                Object next = current.computeIfAbsent(keys[i], k -> new LinkedHashMap<String, Object>());
                if (next instanceof Map) {
                    current = (Map<String, Object>) next;
                }
            }
            current.put(keys[keys.length - 1], entry.getValue());
        }
        return result;
    }
}