package example.user.impl;

import lombok.*;
import example.user.User;
import example.user.role.Role;
import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
public class UserImpl implements User<UUID, UUID> {
    private UUID identifier;
    private String name;
    private Role<UUID> role;

    public UserImpl(UUID identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    @Override
    public Optional<Role<UUID>> getRole() {
        return Optional.ofNullable(role);
    }

    @Override
    public void updateRole(Role<UUID> newRole) {
        this.role = newRole;
    }
}