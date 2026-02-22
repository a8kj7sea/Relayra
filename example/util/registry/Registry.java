package example.util.registry;

import lombok.NonNull;
import example.util.Pair;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Registry<K, V> {

    void register(@NonNull K key, @NonNull V value);

    void unregister(@NonNull K key);

    boolean hasEntry(@NonNull K key);

    @NonNull
    Optional<V> get(@NonNull K key);

    @NonNull
    Map<K, V> asMap();

    @NonNull
    Iterable<Pair<K, V>> entries();

    /**
     * Executes an action for every entry in the registry.
     */
    default void forEach(@NonNull BiConsumer<K, V> action) {
        asMap().forEach(action);
    }

    /**
     * Finds the first value that matches a specific condition.
     */
    default Optional<V> find(@NonNull Predicate<V> predicate) {
        return asMap().values().stream().filter(predicate).findFirst();
    }

    /**
     * Returns a stream of all values in the registry.
     */
    default Stream<V> stream() {
        return asMap().values().stream();
    }

    /**
     * Returns the current size of the registry.
     */
    default int size() {
        return asMap().size();
    }
}