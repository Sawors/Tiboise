package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class IdentifiedItem extends TiboiseItem{

    public void setIdentifier(UUID identifier){
        this.addData(getIdentifierKey(), identifier.toString());
    }

    public static void setItemIdentifier(ItemStack item, UUID identifier){
        if(item == null){
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta != null){
            meta.getPersistentDataContainer().set(getIdentifierKey(), PersistentDataType.STRING, identifier.toString());
        }
    }

    public static @Nullable UUID getItemIdentifier(ItemStack item){
        if(item == null){
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        UUID id = null;
        if(meta != null){
            String identifier = meta.getPersistentDataContainer().get(getIdentifierKey(), PersistentDataType.STRING);
            if(identifier != null){
                try{
                    id = UUID.fromString(identifier);
                } catch (IllegalArgumentException ignored){}
            }
        }

        return id;
    }

    public static NamespacedKey getIdentifierKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "identifier");
    }
}
