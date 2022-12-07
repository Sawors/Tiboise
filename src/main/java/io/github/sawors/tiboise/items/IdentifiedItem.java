package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public abstract class IdentifiedItem extends TiboiseItem{

    public void setIdentifier(String identifier){
        this.addData(getIdentifierKey(), identifier);
    }

    public static void setItemIdentifier(ItemStack item, String identifier){
        if(item == null){
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta != null){
            meta.getPersistentDataContainer().set(getIdentifierKey(), PersistentDataType.STRING, identifier);
        }
    }

    public static @Nullable String getItemIdentifier(ItemStack item){
        if(item == null){
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta != null){
             return meta.getPersistentDataContainer().get(getIdentifierKey(), PersistentDataType.STRING);
        }

        return null;
    }

    public static NamespacedKey getIdentifierKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "identifier");
    }
}
