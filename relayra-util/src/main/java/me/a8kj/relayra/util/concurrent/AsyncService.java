package me.a8kj.relayra.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;


public abstract class AsyncService {


    private static final Executor SERVICE_EXECUTOR = new ForkJoinPool(
            Math.min(Short.MAX_VALUE, Runtime.getRuntime().availableProcessors()),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true
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
}