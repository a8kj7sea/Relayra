package example;

import example.listener.PaymentListener;
import example.payment.impl.RolePayment;
import me.a8kj.relayra.api.Relayra;
import me.a8kj.relayra.core.RelayraProvider;
import example.payment.Payment;
import example.shop.ShopAPI;
import example.user.role.RoleRegistry;
import example.user.role.impl.RoleImpl;

import java.util.UUID;

public class PaymentTest {

    public static RoleRegistry roleRegistry = new RoleRegistry();

    public static void main(String[] args) throws Exception {


        roleRegistry.register(UUID.randomUUID(), new RoleImpl("PREMIUM", UUID.randomUUID()));

        Relayra relayra = RelayraProvider.init("shop-system", "./config");

        ShopAPI shopClient = relayra.createCommandClient(ShopAPI.class);

        ShopAPI publisher = relayra.createPublisher(ShopAPI.class);

        relayra.subscribe(new PaymentListener(shopClient, roleRegistry));

        Payment payment = new RolePayment(UUID.randomUUID(), "a8kj", "PREMIUM");

        publisher.publishPayment((RolePayment) payment);
        Thread.sleep(3000);
        relayra.shutdown();
    }
}