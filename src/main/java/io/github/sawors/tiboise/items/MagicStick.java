package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.core.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class MagicStick extends TiboiseItem implements Listener {
    public MagicStick() {
        super();

        setMaterial(Material.STICK);
    }

    @EventHandler
    public static void testListener(PlayerInteractEvent event){

    }
}
