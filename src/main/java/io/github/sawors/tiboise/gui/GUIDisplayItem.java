package io.github.sawors.tiboise.gui;

import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUIDisplayItem {
    private final String type;
    // this is only to make it compatible with TiboiseItem.getId(ItemStack)
    private final String TIBOISE_ITEM_ID = "gui_button_item";
    private Material material;
    private Component name;
    private List<Component> lore = new ArrayList<>();
    private boolean glint = false;
    private final String uniqueDisplayItemId = RandomStringUtils.randomAlphanumeric(8);
    
    public GUIDisplayItem(String name, Material displayMaterial){
        this.type = name;
        this.name = Component.text("name").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        this.material = displayMaterial;
    }
    
    public ItemStack get(){
        ItemStack item = new ItemStack(material);
        item.editMeta(m -> {
            m.displayName(name);
            m.lore(lore);
            if(glint){
                m.addEnchant(Enchantment.DURABILITY,1,true);
                m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
        ItemMeta meat = item.getItemMeta();
        meat.getPersistentDataContainer().set(getDisplayIdKey(), PersistentDataType.STRING, uniqueDisplayItemId);
        meat.getPersistentDataContainer().set(TiboiseItem.getItemIdKey(), PersistentDataType.STRING, TiboiseItem.formatTextToId(TIBOISE_ITEM_ID));
        meat.getPersistentDataContainer().set(getDisplayTypeKey(), PersistentDataType.STRING, type.toUpperCase(Locale.ROOT));
        item.setItemMeta(meat);
        
        return item;
    }
    
    public static @Nullable String getDisplayItemType(ItemStack item){
        return item.getItemMeta().getPersistentDataContainer().get(getDisplayTypeKey(),PersistentDataType.STRING);
    }
    
    public static @Nullable String getDisplayItemId(ItemStack item){
        return item.getItemMeta().getPersistentDataContainer().get(getDisplayIdKey(),PersistentDataType.STRING);
    }
    
    public GUIDisplayItem setMaterial(Material material) {
        this.material = material;
        return this;
    }
    
    public GUIDisplayItem setName(Component name) {
        this.name = name;
        return this;
    }
    
    public GUIDisplayItem setLore(List<Component> lore) {
        this.lore = lore;
        return this;
    }
    
    public GUIDisplayItem setGlint(boolean glint){
        this.glint = glint;
        return this;
    }
    
    public static NamespacedKey getDisplayTypeKey(){
        return new NamespacedKey(Main.getPlugin(),"display_type");
    }
    protected static NamespacedKey getDisplayIdKey(){
        return new NamespacedKey(Main.getPlugin(),"display_id");
    }
}
