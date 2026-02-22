package me.a8kj.relayra.config.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.a8kj.config.ConfigAPI;
import me.a8kj.config.template.registry.ConfigRegistry;
import me.a8kj.eventbus.manager.EventManager;


@RequiredArgsConstructor
@Getter
public class RelayraConfigAPI implements ConfigAPI {
    private final EventManager eventManager;
    private final ConfigRegistry configRegistry;
}
