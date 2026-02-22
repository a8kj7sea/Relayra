package me.a8kj.relayra.api.distribution;

public record RemoteChannel<T>(String name, Class<T> type) {
}