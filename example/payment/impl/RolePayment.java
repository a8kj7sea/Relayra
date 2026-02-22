package example.payment.impl;

import lombok.*;
import example.payment.Payment;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePayment implements Payment<UUID, UUID, UUID> {
    private UUID identifier;
    private String userName;
    private String paidElementName;
}