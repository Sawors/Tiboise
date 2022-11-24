package io.github.sawors.tiboise.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public abstract class IconItem {
    Component name;
    ArrayList<Component> lore;
    HashSet<String> tags;
    String variant;
    String id;
    Material basematerial;
    HashMap<NamespacedKey, String> additionaldata = new HashMap<>();
    
    public ItemStack get(){
        ItemStack item = new ItemStack(basematerial);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(name);
        meta.lore(lore);
        meta.getPersistentDataContainer().set(TiboiseItem.getItemIdKey(), PersistentDataType.STRING, id.toLowerCase(Locale.ROOT));
        StringBuilder typeskey = new StringBuilder();
        for(String s : tags){
            typeskey.append(s.toUpperCase(Locale.ROOT)).append(":");
        }
        if(typeskey.toString().endsWith(":")){
            typeskey.deleteCharAt(typeskey.lastIndexOf(":"));
        }
        meta.getPersistentDataContainer().set(TiboiseItem.getItemVariantKey(), PersistentDataType.STRING, variant.toLowerCase(Locale.ROOT));
        meta.getPersistentDataContainer().set(TiboiseItem.getItemTagsKey(), PersistentDataType.STRING, typeskey.toString().toLowerCase(Locale.ROOT));
        for(Map.Entry<NamespacedKey, String> entry : additionaldata.entrySet()){
            meta.getPersistentDataContainer().set(entry.getKey(), PersistentDataType.STRING,entry.getValue());
        }
        
        item.setItemMeta(meta);
        
        return item;
    }
}
