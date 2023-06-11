package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;

public class PostPackage extends TiboiseItem {
    
    public PostPackage(){
        setMaterial(Material.PAPER);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
    }
}
