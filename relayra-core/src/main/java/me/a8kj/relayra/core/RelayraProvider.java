package me.a8kj.relayra.core;

import lombok.Getter;
import lombok.NonNull;
import me.a8kj.config.ConfigProvider;
import me.a8kj.config.template.memory.impl.PairedDataMemory;
import me.a8kj.config.template.registry.ConfigRegistry;
import me.a8kj.eventbus.manager.EventManager;
import me.a8kj.logging.Log;
import me.a8kj.logging.impl.ConsoleLogger;
import me.a8kj.relayra.api.Relayra;
import me.a8kj.relayra.api.command.annotation.RelayCommand;
import me.a8kj.relayra.api.connection.ConnectionSource;
import me.a8kj.relayra.api.distribution.ChannelMediator;
import me.a8kj.relayra.api.distribution.RelayBus;
import me.a8kj.relayra.api.messenger.annotation.RedisPublish;
import me.a8kj.relayra.api.queue.RelayQueue;
import me.a8kj.relayra.config.impl.RelayraConfigAPI;
import me.a8kj.relayra.config.impl.RelayraConfigFactory;
import me.a8kj.relayra.config.impl.schema.ConfigSchema;
import me.a8kj.relayra.core.command.RelayCommandExecutor;
import me.a8kj.relayra.core.command.agent.RelayCommandInterceptor;
import me.a8kj.relayra.core.command.strategy.CommandRegistry;
import me.a8kj.relayra.core.connection.lettuce.LettuceConnectionSource;
import me.a8kj.relayra.core.distribution.LettuceRelayBus;
import me.a8kj.relayra.core.distribution.RelayMediator;
import me.a8kj.relayra.core.messenger.RelayMessengerAgent;
import me.a8kj.relayra.core.messenger.agent.RelayPublishInterceptor;
import me.a8kj.relayra.core.queue.LettuceRelayQueue;
import me.a8kj.relayra.core.queue.agent.RelayraQueueAgent;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;


@Getter
public final class RelayraProvider implements Relayra {

    private final ConnectionSource connectionSource;
    private final RelayBus bus;
    private final ChannelMediator mediator;
    private final String configName;
    private final RelayraQueueAgent queueAgent = new RelayraQueueAgent(this);
    protected RelayCommandExecutor commandExecutor;
    private RelayMessengerAgent messengerAgent;


    public static Relayra init(@NonNull String configName, String path) {
        try {
            ConfigProvider.provide();
        } catch (IllegalStateException e) {
            ConfigProvider.load(new RelayraConfigAPI(new EventManager(), new ConfigRegistry()));
        }
        PairedDataMemory<String> memory = RelayraConfigFactory.create(configName, path);
        if (ConfigSchema.LOGGER_ENABLED.fetch(memory) == true) Log.addDestination(new ConsoleLogger());
        return new RelayraProvider(memory, configName);
    }

    private RelayraProvider(@NonNull PairedDataMemory<String> config, String configName) {
        this.configName = configName;
        this.messengerAgent = new RelayMessengerAgent();
        this.connectionSource = new LettuceConnectionSource(config);
        this.mediator = new RelayMediator();
        this.bus = new LettuceRelayBus(connectionSource, mediator);
        this.commandExecutor = new RelayCommandExecutor(connectionSource);
        this.connectionSource.getPubSub().addListener(messengerAgent);

    }

    @Override
    public void shutdown() {
        try {

            this.connectionSource.close();
            this.queueAgent.close();
            RelayraConfigFactory.update(configName);
        } catch (Exception e) {
            throw new RuntimeException("Error during Relayra shutdown/save", e);
        }
    }

    @Override
    public <T> RelayQueue<T> getQueue(String name, Class<T> type) {
        return new LettuceRelayQueue<>(name, type, this.connectionSource);
    }

    @Override
    public void registerQueueListeners(Object... listeners) {
        for (Object listener : listeners) {
            queueAgent.register(listener);
        }
    }

    @Override
    public <T> T createCommandClient(Class<T> clientInterface) {
        try {
            return new ByteBuddy()
                    .subclass(clientInterface)
                    .method(ElementMatchers.isAnnotatedWith(RelayCommand.class))
                    .intercept(MethodDelegation.to(new RelayCommandInterceptor(this.commandExecutor)))
                    .make()
                    .load(clientInterface.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate dynamic command client for: " + clientInterface.getName(), e);
        }
    }

    @Override
    public void subscribe(Object listener) {
        messengerAgent.register(listener);
        this.connectionSource.getPubSub().sync().subscribe(messengerAgent.getSubscribedChannels());
    }

    @Override
    public <T> T createPublisher(Class<T> publisherInterface) {
        try {
            return new ByteBuddy()
                    .subclass(publisherInterface)
                    .method(ElementMatchers.isAnnotatedWith(RedisPublish.class))
                    .intercept(MethodDelegation.to(new RelayPublishInterceptor(this.connectionSource)))
                    .make()
                    .load(publisherInterface.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CommandRegistry getCommandRegistry() {
        return this.commandExecutor.getRegistry();
    }
}
