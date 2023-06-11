package io.github.sawors.tiboise.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface ItemCarrier {
    
    NamespacedKey getStorageDataKey();
    ItemStack[] getContent();
    void storeContent(ItemStack[] content);
}
