package me.a8kj.relayra.codec;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Deserializer<T, S> {

    @Nullable
    T deserialize(@Nullable S data);
}