package me.a8kj.relayra.api.command.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RelayCommand {
    String command();

    String key();
}
