package example.util;

/**
 * A generic, immutable data carrier (Record) that holds two related objects.
 * Commonly used to represent a key-value pair, an entry in a registry,
 * or a simple association between two different data types.
 *
 * @param <K> the type of the first element (the key).
 * @param <V> the type of the second element (the value).
 * @author <a href="https://github.com/a8kj7sea">a8kj7sea</a>
 * @since 0.1
 */
public record Pair<K, V>(K key, V value) {

    /**
     * Creates a new immutable Pair instance.
     *
     * @param <K>   the key type.
     * @param <V>   the value type.
     * @param key   the first element of the pair.
     * @param value the second element of the pair.
     * @return a new {@link Pair} instance.
     * @author <a href="https://github.com/a8kj7sea">a8kj7sea</a>
     * @since 0.1
     */
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}