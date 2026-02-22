package me.a8kj.relayra.codec.impl;

import lombok.experimental.UtilityClass;
import me.a8kj.relayra.codec.CodecAdapter;

@UtilityClass
public class JsonCodecFactory {

    public static <T> CodecAdapter<T, String> create(Class<T> clazz) {
        JsonCodec<T> codec = new JsonCodec<>(clazz);
        return new CodecAdapter<>(codec, codec);
    }
}