package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.Tiboise;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TiboiseItem {



    // When creating a new object NEVER FORGET to register it in main class Stones on onEnable using Tiboise.registerItem(StonesItem item)
    Component name;
    ArrayList<Component> lore;
    HashSet<String> tags;
    String variant;
    String id;
    boolean unique;
    Material basematerial;
    HashMap<NamespacedKey, String> additionaldata = new HashMap<>();

    public TiboiseItem(){
        String classname = this.getClass().getSimpleName();
        id = getTypeId();
        tags = new HashSet<>();
        lore = new ArrayList<>();
        variant = ItemVariant.DEFAULT.getFormatted();

        StringBuilder nameformated = new StringBuilder();
        char lastchar = '/';
        for(char c : classname.toCharArray()){
            if(Character.isUpperCase(c) && Character.isLowerCase(lastchar)){
                nameformated.append(" ");
            }
            nameformated.append(c);
            lastchar = c;
        }
        name = Component.translatable(ChatColor.WHITE + nameformated.toString());

        basematerial = Material.ROTTEN_FLESH;
        unique = false;
    }

    public TiboiseItem(String variant){
        this();
        this.variant = formatTextToId(variant);
    }

    public String getId(){
        return id;
    }

    public String getTypeId(){
        return formatTextToId(getClass().getSimpleName());
    }

    public void addData(@NotNull NamespacedKey key, String data){
        additionaldata.put(key,data);
    }

    public void setVariant(String variant){
        this.variant = formatTextToId(variant);
    }

    public String getVariant(){
        return this.variant;
    }

    public void setDisplayName(Component name){
        this.name = name;
    }
    public void setId(String id){
        this.id = id;
    }

    public void setLore(ArrayList<Component> lore){
        this.lore.addAll(lore);
    }

    public void setUnique(boolean unique){
        this.unique = unique;
    }

    public void setMaterial(Material material){
        if(material.isItem()){
            this.basematerial = material;
        }
    }

    public void addTag(ItemTag type){
        tags.add(type.toString().toLowerCase(Locale.ROOT));
    }

    public ItemStack get(){
        ItemStack item = new ItemStack(basematerial);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name);
        meta.lore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(TiboiseItem.getItemIdKey(), PersistentDataType.STRING, id.toLowerCase(Locale.ROOT));
        StringBuilder typeskey = new StringBuilder();
        for(String s : tags){
            typeskey.append(s.toUpperCase(Locale.ROOT)).append(":");
        }
        if(typeskey.toString().endsWith(":")){
            typeskey.deleteCharAt(typeskey.lastIndexOf(":"));
        }
        meta.getPersistentDataContainer().set(TiboiseItem.getItemTagsKey(), PersistentDataType.STRING, typeskey.toString().toLowerCase(Locale.ROOT));
        for(Map.Entry<NamespacedKey, String> entry : additionaldata.entrySet()){
            meta.getPersistentDataContainer().set(entry.getKey(), PersistentDataType.STRING,entry.getValue());
        }

        item.setItemMeta(meta);

        return item;
    }


    public String getPersistentDataPrint(){
        PersistentDataContainer container = this.get().getItemMeta().getPersistentDataContainer();
        StringBuilder printed = new StringBuilder();

        printed
                .append("Item ID : ").append(container.get(TiboiseItem.getItemIdKey(), PersistentDataType.STRING))
                .append("\n")
                .append("Item Tags : ").append("[")
        ;
        String typestr = container.get(TiboiseItem.getItemTagsKey(), PersistentDataType.STRING);
        if(typestr != null){
            if(typestr.contains(":")){
                for(String str : typestr.split(":")){
                    printed.append("\n  - ").append(str);
                }
            } else {
                printed.append(typestr);
            }

            printed.append("\n");
        }
        printed.append("]");

        return printed.toString();
    }

    public static NamespacedKey getItemIdKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "id");
    }

    public static NamespacedKey getItemTagsKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "tags");
    }

    public static NamespacedKey getItemVariantKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "variant");
    }

    public static String getItemId(ItemStack item){
        String itemid = getItemData(item, getItemIdKey());
        if(itemid == null || itemid.length() == 0){
            itemid = item.getType().toString().toLowerCase(Locale.ROOT);
        }
        return itemid;
    }
    public static List<String> getItemTags(ItemStack item){
        String foundtags = getItemData(item, getItemTagsKey());
        List<String> tags = List.of();
        if(foundtags != null && foundtags.length() > 0){
            if(foundtags.contains(":")){
                tags = List.of(foundtags.split(":"));
            } else {
                tags = List.of(foundtags);
            }
        }

        return tags;
    }
    public static String getItemVariant(ItemStack item){
        String data = getItemData(item, getItemVariantKey());
        return data != null ? data : ItemVariant.DEFAULT.getFormatted();
    }
    private static @Nullable String getItemData(ItemStack item, NamespacedKey key){
        if(item == null){
            return null;
        }
        String data = null;
        if(item.hasItemMeta()){
            String checkdata = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if(checkdata != null){
                data = checkdata;
            }
        }
        return data;
    }

    public static String formatTextToId(String text){
        StringBuilder idformated = new StringBuilder();
        char lastchar = '/';
        for(char c : text.replaceAll(" ", "_").toCharArray()){
            if(Character.isUpperCase(c) && Character.isLowerCase(lastchar)){
                idformated.append("_");
            }
            idformated.append(Character.toLowerCase(c));
            lastchar = c;
        }
        return idformated.toString();
    }

    public static String formatTextToName(String text){
        StringBuilder nameformated = new StringBuilder();
        char lastchar = '/';
        for(char c : text.toCharArray()){
            if(Character.isUpperCase(c) && Character.isLowerCase(lastchar)){
                nameformated.append(" ");
            }
            nameformated.append(c);
            lastchar = c;
        }

        return nameformated.toString();
    }

}
