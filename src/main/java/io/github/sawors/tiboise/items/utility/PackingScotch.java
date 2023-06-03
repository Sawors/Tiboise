package io.github.sawors.tiboise.items.utility;

import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import java.util.List;

public class PackingScotch extends TiboiseItem implements Listener {
    
    public PackingScotch(){
        setMaterial(Material.SLIME_BALL);
        setLore(List.of(
                Component.text("Use it to pack storage blocks."),
                Component.text("Packed storage blocks can be broken"),
                Component.text("and moved without losing their content.")
        ));
    }
}
