package me.a8kj.relayra.util.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A fluent, high-level wrapper for {@link CompletableFuture}.
 * Provides enhanced error handling, timeouts, and functional chaining.
 *
 *  INSPIRED BY @Mqzn
 * @param <T> The type of the result.
 */
public final class Promise<T> {

    private final CompletableFuture<T> future;

    private Promise(CompletableFuture<T> future) {
        this.future = future;
    }

    // --- Static Factories ---

    public static <T> Promise<T> of(@NotNull CompletableFuture<T> future) {
        return new Promise<>(future);
    }

    public static <T> Promise<T> supply(@NotNull Supplier<T> supplier) {
        return of(CompletableFuture.supplyAsync(supplier));
    }

    public static <T> Promise<T> supply(@NotNull Supplier<T> supplier, @NotNull Executor executor) {
        return of(CompletableFuture.supplyAsync(supplier, executor));
    }

    public static <T> Promise<T> completed(@Nullable T value) {
        return of(CompletableFuture.completedFuture(value));
    }

    // --- Callback Logic ---

    public Promise<T> onSuccess(@NotNull Consumer<T> action) {
        future.thenAccept(action);
        return this;
    }

    public Promise<T> onSuccess(@NotNull Runnable action) {
        future.thenRun(action);
        return this;
    }

    public Promise<T> onError(@NotNull Consumer<Throwable> handler) {
        future.exceptionally(ex -> {
            handler.accept(ex);
            return null;
        });
        return this;
    }

    /**
     * Handles both success and error in a single block.
     */
    public Promise<T> handle(@NotNull Consumer<T> onSuccess, @NotNull Consumer<Throwable> onError) {
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                onError.accept(ex);
            } else {
                onSuccess.accept(result);
            }
        });
        return this;
    }

    // --- Transformation ---

    public <U> Promise<U> map(@NotNull Function<T, U> mapper) {
        return of(future.thenApply(mapper));
    }

    public <U> Promise<U> flatMap(@NotNull Function<T, CompletableFuture<U>> mapper) {
        return of(future.thenCompose(mapper));
    }

    // --- Timeouts ---

    /**
     * Fails the promise with a TimeoutException if it takes too long.
     */
    public Promise<T> orTimeout(long value, TimeUnit unit) {
        future.orTimeout(value, unit);
        return this;
    }

    /**
     * Completes the promise with a fallback value if it takes too long.
     */
    public Promise<T> timeout(long value, TimeUnit unit, T fallbackValue) {
        future.completeOnTimeout(fallbackValue, value, unit);
        return this;
    }

    // --- Utilities ---

    /**
     * Prints the stack trace if an exception occurs.
     */
    public Promise<T> traceErrors() {
        return onError(Throwable::printStackTrace);
    }

    /**
     * Provides a fallback value if an error occurs.
     */
    public Promise<T> fallback(@NotNull Function<Throwable, T> fallbackFunc) {
        return of(future.exceptionally(fallbackFunc));
    }

    /**
     * Runs an action regardless of success or failure.
     */
    public Promise<T> always(@NotNull Runnable action) {
        future.whenComplete((r, ex) -> action.run());
        return this;
    }

    /**
     * Blocks the current thread and returns the result.
     */
    public T join() {
        return future.join();
    }

    /**
     * Returns the underlying CompletableFuture.
     */
    public CompletableFuture<T> unwrap() {
        return future;
    }
}