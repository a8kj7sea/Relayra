package example.payment;

import example.util.keyed.Identifiable;
// i hate my job
public interface Payment<ID, UserId, RoleID> extends Identifiable<ID> {


    // ale eshtra
    String getUserName();

    String getPaidElementName();

    default ID getTransactionIdentifier() {
        return getIdentifier();
    }
}
