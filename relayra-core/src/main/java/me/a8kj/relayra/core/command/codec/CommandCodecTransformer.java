package me.a8kj.relayra.core.command.codec;


import me.a8kj.relayra.codec.CodecAdapter;
import me.a8kj.relayra.codec.impl.JsonCodecFactory;

import java.lang.reflect.Type;

public class CommandCodecTransformer {

    public Object[] encodeArguments(Object[] args) {
        if (args == null) return new Object[0];
        Object[] encoded = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            encoded[i] = (args[i] == null || isSimple(args[i]))
                    ? args[i]
                    : JsonCodecFactory.create((Class<Object>) args[i].getClass()).serialize(args[i]);
        }
        return encoded;
    }

    public Object decodeResult(Object raw, Type targetType) {
        if (raw == null || targetType == void.class || targetType == Void.class) return null;
        if (raw instanceof String && !targetType.equals(String.class)) {
            CodecAdapter<Object, String> adapter = JsonCodecFactory.create((Class<Object>) targetType);
            return adapter.deserialize((String) raw);
        }
        return raw;
    }

    private boolean isSimple(Object obj) {
        Class<?> c = obj.getClass();
        return c.isPrimitive() || c == String.class || Number.class.isAssignableFrom(c) || c == Boolean.class;
    }
}