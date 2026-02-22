package me.a8kj.relayra.codec.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonCodecRegistry {

    private static final JsonCodecRegistry INSTANCE = new JsonCodecRegistry();
    private final Map<Type, Object> globalAdapters = new ConcurrentHashMap<>();
    private volatile Gson sharedGson;

    public static JsonCodecRegistry getInstance() {
        return INSTANCE;
    }

    public JsonCodecRegistry register(Type type, Object adapter) {
        globalAdapters.put(type, adapter);
        sharedGson = null;
        return this;
    }

    public Gson getGson() {
        if (sharedGson == null) {
            synchronized (this) {
                if (sharedGson == null) {
                    GsonBuilder builder = new GsonBuilder().setLenient();
                    globalAdapters.forEach(builder::registerTypeAdapter);
                    sharedGson = builder.create();
                }
            }
        }
        return sharedGson;
    }
}