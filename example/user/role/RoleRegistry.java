package example.user.role;

import lombok.NonNull;
import example.util.Pair;
import example.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A registry for managing and caching roles.
 * Keyed by UUID (or the Role identifier type).
 */
public class RoleRegistry implements Registry<UUID, Role<UUID>> {

    private final Map<UUID, Role<UUID>> roles = new ConcurrentHashMap<>();

    @Override
    public void register(@NotNull UUID key, @NotNull Role<UUID> value) {
        this.roles.put(key, value);
    }

    @Override
    public void unregister(@NotNull UUID key) {
        this.roles.remove(key);
    }

    @Override
    public boolean hasEntry(@NotNull UUID key) {
        return roles.containsKey(key);
    }

    @Override
    public @NonNull Optional<Role<UUID>> get(@NotNull UUID key) {
        return Optional.ofNullable(roles.get(key));
    }

    @Override
    public @NonNull Map<UUID, Role<UUID>> asMap() {
        return Collections.unmodifiableMap(roles);
    }

    @Override
    public @NonNull Iterable<Pair<UUID, Role<UUID>>> entries() {
        return roles.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Finds a role by its name (e.g., "Admin", "Default").
     * * @param name The name to search for.
     * @return An optional containing the role if found.
     */
    public Optional<Role<UUID>> findByName(String name) {
        return roles.values().stream()
                .filter(role -> role.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public int size() {
        return roles.size();
    }
}