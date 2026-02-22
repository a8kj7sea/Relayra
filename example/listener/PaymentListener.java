package example.listener;

import lombok.RequiredArgsConstructor;
import me.a8kj.relayra.api.messenger.annotation.RedisSubscribe;
import example.payment.impl.RolePayment;
import example.user.role.RoleRegistry;
import example.shop.ShopAPI;
import example.user.User;
import example.user.impl.UserImpl;

import java.util.UUID;

@RequiredArgsConstructor
public class PaymentListener {

    private final ShopAPI shopAPI;
    private final RoleRegistry roleRegistry;

    @RedisSubscribe(channel = "shop:payments")
    public void onPayment(RolePayment payment) {
        System.out.println("Processing Payment: " + payment.getIdentifier());

        shopAPI.getUser(payment.getUserName()).onSuccess(fetchedUser -> {

            User<UUID, UUID> user = (fetchedUser != null) ? fetchedUser : new UserImpl(UUID.randomUUID(), payment.getUserName());

            user.updateRole(roleRegistry.findByName(payment.getPaidElementName()).orElse(null));

            shopAPI.saveUser(user.getName(), (UserImpl) user).onSuccess(v -> {
                System.out.println("Role updated for " + user.getName() + " to " + user.getRole());
            });
        });
    }
}