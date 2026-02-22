package example.user.role;

import example.util.keyed.Identifiable;

import java.util.List;

public interface Role<ID> extends Identifiable<ID> {

    String getName();

    List<String> getPermissions();

    List<Object> getPerks();

    void updatePermissions(List<String> perms);

    void updatePerks(List<Object> perks);
}
