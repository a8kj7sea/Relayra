package me.a8kj.relayra.codec.impl;

import lombok.RequiredArgsConstructor;
import me.a8kj.relayra.codec.Deserializer;
import me.a8kj.relayra.codec.Serializer;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class JsonCodec<T> implements Serializer<T, String>, Deserializer<T, String> {

    private final Class<T> type;

    @Override
    @Nullable
    public String serialize(@Nullable T object) {
        if (object == null) return null;
        return JsonCodecRegistry.getInstance().getGson().toJson(object);
    }

    @Override
    @Nullable
    public T deserialize(@Nullable String data) {
        if (data == null || data.isEmpty()) return null;
        return JsonCodecRegistry.getInstance().getGson().fromJson(data, type);
    }
}