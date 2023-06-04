package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.ConfigModules;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.ItemTag;
import io.github.sawors.tiboise.core.ItemVariant;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.integrations.voicechat.PortableRadio;
import io.github.sawors.tiboise.items.tools.radius.Excavator;
import io.github.sawors.tiboise.items.tools.radius.Hammer;
import io.github.sawors.tiboise.items.tools.tree.Broadaxe;
import io.github.sawors.tiboise.items.utility.PackingScotch;
import io.github.sawors.tiboise.items.utility.PortableCraftingTable;
import io.github.sawors.tiboise.items.utility.coppercompass.CopperCompass;
import io.github.sawors.tiboise.post.PostEnvelope;
import io.github.sawors.tiboise.post.PostEnvelopeClosed;
import io.github.sawors.tiboise.post.PostStamp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public abstract class TiboiseItem {
    
    //
    //  ITEM REGISTERING
    //
    private static Map<String, TiboiseItem> itemmap = new HashMap<>();
    private static Set<Integer> registeredlisteners = new HashSet<>();
    
    //
    // Register items here !
    public static void loadItems(){
        registerItem(new MagicStick());
        registerItem(new Hammer());
        registerItem(new Excavator());
        registerItem(new Broadaxe());
        registerItem(new CopperCompass());
        registerItem(new TiboiseWrench());
        registerItem(new PortableCraftingTable());
        registerItem(new PackingScotch());
        registerItem(new PostEnvelope());
        registerItem(new PostEnvelopeClosed());
        registerItem(new PostStamp());
        
        if(Tiboise.isModuleEnabled(ConfigModules.ECONOMY)){
            registerItem(new CoinItem());
        }
        if(Tiboise.isVoiceChatEnabled()){
            registerItem(new PortableRadio());
        }
    }
    
    private static void registerItem(TiboiseItem item){
        
        item.onRegister();
        
        itemmap.put(item.getId(), item);
        
        Recipe defaultrecipe = item.getRecipe();
        if(defaultrecipe != null) Bukkit.addRecipe(defaultrecipe);
        for(ItemVariant var : item.getPossibleVariants()){
            Recipe variantrecipe = item.getRecipe(var);
            if(variantrecipe != null) Bukkit.addRecipe(variantrecipe);
        }
        
        if(item instanceof Listener listener){
            for(Method method : listener.getClass().getMethods()){
                if(!registeredlisteners.contains(method.hashCode()) && method.getAnnotation(EventHandler.class) != null && method.getParameters().length >= 1 && Event.class.isAssignableFrom(method.getParameters()[0].getType())){
                    // method is recognized as handling an event
                    /*
                    plugin -> parameter
                    listener -> parameter
                    for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
                        getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
                    }
                    */
                    Class<? extends Event> itemclass = method.getParameters()[0].getType().asSubclass(Event.class);
                    
                    Bukkit.getServer().getPluginManager().registerEvent(itemclass, listener, method.getAnnotation(EventHandler.class).priority(), EventExecutor.create(method, itemclass), Tiboise.getPlugin());
                    registeredlisteners.add(method.hashCode());
                }
            }
        }
    }
    
    public static @Nullable TiboiseItem getRegisteredItem(String id){
        return itemmap.get(id);
    }
    
    public static Inventory getItemListDisplay(){
        Inventory itemsview = Bukkit.createInventory(null, 6*9, Component.text("Item List"));
        for(TiboiseItem item : itemmap.values()){
            itemsview.addItem(item.get());
        }
        
        return itemsview;
    }
    
    
    //
    // INSTANTIATION DATA
    //
    Component name;
    List<Component> lore;
    HashSet<String> tags;
    String variant;
    String id;
    boolean unique;
    Material basematerial;
    Map<NamespacedKey, String> additionaldata = new HashMap<>();
    // just added this in case we need to create items with durability
    // setting this to true will give the item its durability stat back
    boolean overwriteunbreakable = true;

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
        name = Component.text(nameformated.toString()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

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
    
    public void setLore(List<Component> lore){
        this.lore = lore;
    }
    
    public void addLore(List<Component> lore){
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

    public void overwriteUnbreakbale(boolean overwrite){
        this.overwriteunbreakable = overwrite;
    }
    
    public NamespacedKey getItemReference(){
        return new NamespacedKey(Tiboise.getPlugin(),this.getId());
    }

    public ItemStack get(){
        ItemStack item = new ItemStack(basematerial);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name);
        meta.lore(lore);
        // just to permit items with durability
        if(!overwriteunbreakable){
            meta.setUnbreakable(true);
        }
        // useless in case we decide to overwrite the unbreakable tag
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
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
        
        meta.setCustomModelData(this.id.hashCode());

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
        return new NamespacedKey(Tiboise.getPlugin(), "id");
    }

    public static NamespacedKey getItemTagsKey(){
        return new NamespacedKey(Tiboise.getPlugin(), "tags");
    }

    public static NamespacedKey getItemVariantKey(){
        return new NamespacedKey(Tiboise.getPlugin(), "variant");
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
    
    public @Nullable Recipe getRecipe(){
        return null;
    }
    
    public @Nullable Recipe getRecipe(ItemVariant variant){
        return null;
    }
    
    public List<ItemVariant> getPossibleVariants(){
        return List.of(
                ItemVariant.DEFAULT
        );
    }
    
    public NamespacedKey getIdAsKey(){
        return new NamespacedKey(Tiboise.getPlugin(),getId());
    }
    
    /**
     *
     * @param item the ItemStack to get the data from
     * @return the corresponding but with the data from the item retrieved
     * Unlike getRegisteredItem, which returns the ItemStack with its data reset to default, this method
     * attempts to clone the data extracted from the ItemStack to the new VbItem.
     */
    public static @Nullable TiboiseItem cloneFromItemStack(ItemStack item){
        if(item == null){
            return null;
        }
        TiboiseItem target = getRegisteredItem(getItemId(item));
        if(target == null){
            return null;
        }
        
        //      Component name;
        //    ArrayList<Component> lore;
        //    HashSet<String> tags;
        //    String variant;
        //    HashMap<NamespacedKey, String> additionaldata = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        // Display Name
        target.setDisplayName(item.displayName());
        // Lore
        target.setLore(meta.lore());
        // Variant
        target.setVariant(container.get(getItemVariantKey(), PersistentDataType.STRING));
        // Tags
        for(String s : getItemTags(item)){
            try{
                target.addTag(ItemTag.valueOf(s));
            } catch (IllegalArgumentException e){
                Bukkit.getLogger().log(Level.INFO, "trying to get an incorrect item tag for item "+target.getId()+" (tag : "+s+")");
            }
        }
        // Additional Data
        for(NamespacedKey key : container.getKeys()){
            if(key.equals(getItemTagsKey()) || key.equals(getItemVariantKey()) || key.equals(getItemIdKey())){
                continue;
            }
            target.addData(key, container.get(key, PersistentDataType.STRING));
        }
        
        
        return target;
    }
    
    public void onRegister(){
        // add a way for inherited items to do something when they are initialized
    }

}
