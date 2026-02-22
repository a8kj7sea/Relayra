package me.a8kj.relayra.codec;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class CodecAdapter<T, S> {

    private final Serializer<T, S> serializer;
    private final Deserializer<T, S> deserializer;

    public S serialize(T obj) {
        return serializer.serialize(obj);
    }

    @Nullable
    public T deserialize(@Nullable S data) {
        return deserializer.deserialize(data);
    }
}