package io.github.sawors.tiboise;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class TiboiseStartEvent extends PluginEnableEvent {
    public TiboiseStartEvent(@NotNull Plugin plugin) {
        super(plugin);
    }
}
