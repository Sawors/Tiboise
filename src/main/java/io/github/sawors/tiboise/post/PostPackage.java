package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.ItemCarrier;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class PostPackage extends TiboiseItem implements ItemCarrier {
    
    public PostPackage(){
        setMaterial(Material.PAPER);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        addTag(ItemTag.POST_SENDABLE);
    }
    
    @Override
    public NamespacedKey getStorageDataKey() {
        return null;
    }
    
    @Override
    public ItemStack[] getContent() {
        return new ItemStack[0];
    }
    
    @Override
    public void storeContent(ItemStack[] content) {
    
    }
}
