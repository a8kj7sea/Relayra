package me.a8kj.relayra.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AsyncService {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 4;
    private static final long KEEP_ALIVE_TIME = 60L;

    private static final Executor SERVICE_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2048),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("relayra-async-worker-" + counter.getAndIncrement());
                    thread.setDaemon(false);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    protected <T> Promise<T> async(@NotNull Catching.Supplier<T> supplier) {
        return Promise.of(CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw (e instanceof RuntimeException rx) ? rx : new CompletionException(e);
            }
        }, SERVICE_EXECUTOR));
    }

    protected Promise<Void> async(@NotNull Catching.Runnable runnable) {
        return Promise.of(CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw (e instanceof RuntimeException rx) ? rx : new CompletionException(e);
            }
        }, SERVICE_EXECUTOR));
    }

    public static Executor getExecutor() {
        return SERVICE_EXECUTOR;
    }
}