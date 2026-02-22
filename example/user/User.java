package example.user;

import example.user.role.Role;
import example.util.keyed.Identifiable;

import java.util.Optional;

public interface User<ID, RoleID> extends Identifiable<ID> {
    String getName();

    Optional<Role<RoleID>> getRole();

    void updateRole(Role<RoleID> newRole);

    default boolean hasRole() {
        return getRole().isPresent();
    }

    default boolean hasPermission(String permission) {
        if (!hasRole()) return false;
        return getRole().map(role ->
                role.getPermissions().contains(permission) ||
                        role.getPermissions().contains("*")
        ).orElse(false);
    }
}
