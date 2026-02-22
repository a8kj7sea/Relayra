package me.a8kj.relayra.util.concurrent;


public interface Catching {

    @FunctionalInterface
    interface Runnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    interface Supplier<V> {
        V get() throws Exception;
    }

    @FunctionalInterface
    interface Consumer<T> {
        void accept(T t) throws Exception;
    }
}