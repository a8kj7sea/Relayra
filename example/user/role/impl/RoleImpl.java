package example.user.role.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import example.user.role.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleImpl implements Role<UUID> {

    private String name;
    private UUID identifier;
    private final List<Object> perks = new ArrayList<>();
    private final List<String> permissions = new ArrayList<>();


    @Override
    public void updatePermissions(List<String> perms) {
        this.permissions.clear();
        this.permissions.addAll(perms);
    }

    @Override
    public void updatePerks(List<Object> perks) {
        this.perks.clear();
        this.perks.addAll(perks);
    }
}
