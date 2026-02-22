package example.shop;

import me.a8kj.relayra.api.command.annotation.RelayCommand;
import me.a8kj.relayra.api.messenger.annotation.RedisPublish;
import example.payment.impl.RolePayment;
import example.user.User;
import example.user.impl.UserImpl;
import me.a8kj.relayra.util.concurrent.Promise;

public interface ShopAPI {

    @RelayCommand(command = "SET", key = "user:{args[0]}")
    Promise<Void> saveUser(String username, UserImpl user);

    @RelayCommand(command = "GET", key = "player:{args[0]}")
    Promise<User> getUser(String username);

    @RedisPublish(channel = "shop:payments")
    Promise<Long> publishPayment(RolePayment payment);
}
