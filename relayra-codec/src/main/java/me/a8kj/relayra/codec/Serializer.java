package me.a8kj.relayra.codec;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Serializer<T, S> {

    @Nullable
    S serialize(@Nullable T object);
}