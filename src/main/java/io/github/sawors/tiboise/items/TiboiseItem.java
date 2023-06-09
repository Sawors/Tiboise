package io.github.sawors.tiboise.items;

import com.google.common.collect.ImmutableSet;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.ItemVariant;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.integrations.voicechat.PortableRadio;
import io.github.sawors.tiboise.items.armor.scuba.DivingBoots;
import io.github.sawors.tiboise.items.armor.scuba.DivingChestplate;
import io.github.sawors.tiboise.items.armor.scuba.DivingHelmet;
import io.github.sawors.tiboise.items.armor.scuba.DivingLeggings;
import io.github.sawors.tiboise.items.hats.Fez;
import io.github.sawors.tiboise.items.hats.Kirby;
import io.github.sawors.tiboise.items.hats.Monocle;
import io.github.sawors.tiboise.items.hats.Sombrero;
import io.github.sawors.tiboise.items.hats.villagers.*;
import io.github.sawors.tiboise.items.tools.AmethystPickaxe;
import io.github.sawors.tiboise.items.tools.radius.Excavator;
import io.github.sawors.tiboise.items.tools.radius.Hammer;
import io.github.sawors.tiboise.items.tools.tree.Broadaxe;
import io.github.sawors.tiboise.items.utility.Flare;
import io.github.sawors.tiboise.items.utility.InkQuill;
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

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public abstract class TiboiseItem {
    
    //
    //  ITEM REGISTERING
    //
    private static final Map<String, TiboiseItem> itemmap = new HashMap<>();
    private static final Set<Integer> registeredlisteners = new HashSet<>();
    private static final Map<Class<? extends TiboiseItem>,String> idClassLink = new HashMap<>();
    
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
        registerItem(new Flare());
        registerItem(new DivingHelmet());
        registerItem(new DivingChestplate());
        registerItem(new DivingLeggings());
        registerItem(new DivingBoots());
        registerItem(new AmethystPickaxe());
        registerItem(new InkQuill());
        
        //HATS
        registerItem(new StrawHat());
        registerItem(new Kirby());
        registerItem(new Sombrero());
        registerItem(new Monocle());
        registerItem(new Fez());
        registerItem(new ArmorerHat());
        registerItem(new FishermanHat());
        registerItem(new FletcherHat());
        registerItem(new WeaponsmithHat());
        
        if(Tiboise.isModuleEnabled(Tiboise.ConfigModules.ECONOMY)){
            registerItem(new CoinItem());
        }
        if(Tiboise.isVoiceChatEnabled()){
            registerItem(new PortableRadio());
        }
        
        
       
       
        // to avoid data piling
        registeredlisteners.clear();
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
        
        idClassLink.put(item.getClass(),item.getId());
        
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
    String helpText;
    final static int LORE_RECOMMENDED_LINE_WIDTH = 28;
    // just added this in case we need to create items with durability
    // setting this to true will give the item its durability stat back
    boolean overwriteunbreakable = true;
    
    private static final String DEFAULT_HELP_TEXT = "This item has no particular mechanic.";

    public TiboiseItem(){
        String classname = this.getClass().getSimpleName();
        id = getTypeId();
        tags = new HashSet<>();
        lore = new ArrayList<>();
        variant = ItemVariant.DEFAULT.getFormatted();
        helpText = DEFAULT_HELP_TEXT;

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

    private String getTypeId(){
        return formatTextToId(getClass().getSimpleName());
    }
    
    public static String getId(Class<? extends TiboiseItem> reference){
        return idClassLink.getOrDefault(reference,formatTextToId(reference.getSimpleName()));
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
    
    // TODO : make this work
    public void setShortLore(String shortLore){
        List<StringBuilder> splitLore = List.of(new StringBuilder(shortLore));
        if(shortLore.length() > LORE_RECOMMENDED_LINE_WIDTH){
            
            final String[] words = shortLore.split(" ");
            int index = 0;
            int line = 0;
            splitLore = new ArrayList<>();
            logAdmin((int)(shortLore.length()/28.0)+1);
            StringBuilder builder = new StringBuilder();
            for(String w : words){
                index += w.length();
                builder.append(w).append(" ");
                if(splitLore.size()<=line){
                    splitLore.add(builder);
                } else {
                    splitLore.set(line,builder);
                }
                if(index >= LORE_RECOMMENDED_LINE_WIDTH){
                        line++;
                        index = 0;
                        builder = new StringBuilder();
                }
            }
        }
        
        List<Component> newLore = new ArrayList<>();
        // there is a better functional way of doing this, but this is simpler
        for(StringBuilder s : splitLore){
            newLore.add(Component.text(s.toString()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        this.lore = newLore;
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
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        StringBuilder typeskey = new StringBuilder();
        for(String s : tags){
            typeskey.append(s.toUpperCase(Locale.ROOT)).append(":");
        }
        if(typeskey.toString().endsWith(":")){
            typeskey.deleteCharAt(typeskey.lastIndexOf(":"));
        }
        // add data to Persistent Data Container
        container.set(getItemIdKey(), PersistentDataType.STRING, id.toLowerCase(Locale.ROOT));
        container.set(getItemVariantKey(), PersistentDataType.STRING, variant.toLowerCase(Locale.ROOT));
        container.set(getItemTagsKey(), PersistentDataType.STRING, typeskey.toString().toLowerCase(Locale.ROOT));
        for(Map.Entry<NamespacedKey, String> entry : additionaldata.entrySet()){
            container.set(entry.getKey(), PersistentDataType.STRING,entry.getValue());
        }
        container.set(getHelpTextKey(),PersistentDataType.STRING,helpText);
        
        meta.setCustomModelData(this.id.hashCode());

        item.setItemMeta(meta);

        return item;
    }


    public static String getPersistentDataPrint(ItemStack item){
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
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
        
        printed.append("\nOther Data :");
        for(NamespacedKey key : container.getKeys()){
            if(key.equals(getItemTagsKey()) || key.equals(getItemTagsKey())) continue;
            printed.append("\n - ").append(key).append(" : ").append(container.get(key,PersistentDataType.STRING));
        }

        return printed.toString();
    }

    protected static NamespacedKey getItemIdKey(){
        return new NamespacedKey(Tiboise.getPlugin(), "id");
    }
    
    protected static NamespacedKey getItemTagsKey(){
        return new NamespacedKey(Tiboise.getPlugin(), "tags");
    }
    
    protected static NamespacedKey getItemVariantKey(){
        return new NamespacedKey(Tiboise.getPlugin(), "variant");
    }
    
    /**
     *
     * @return The NamespacedKey refering to this item, usefull to register recipes.
     */
    public NamespacedKey getItemKey(){
        return new NamespacedKey(Tiboise.getPlugin(),getId());
    }

    public static @NotNull String getItemId(ItemStack item){
        if(item == null) return "null";
        String itemid = getItemData(item, getItemIdKey());
        if(itemid == null || itemid.length() == 0){
            itemid = item.getType().toString().toLowerCase(Locale.ROOT);
        }
        return itemid;
    }
    
    public static boolean isTiboiseItem(ItemStack item){
        if(item == null) return false;
        String itemId = getItemData(item, getItemIdKey());
        return !(itemId == null || itemId.length() == 0);
    }
    
    public static Set<String> getItemTags(ItemStack item){
        return deserializeTags(getItemData(item, getItemTagsKey()));
    }
    
    private static ImmutableSet<String> deserializeTags(String source){
        ImmutableSet<String> tags = ImmutableSet.of();
        if(source != null && source.length() > 0){
            source = source.toLowerCase(Locale.ROOT);
            if(source.contains(":")){
                tags = ImmutableSet.copyOf(source.split(":"));
            } else {
                tags = ImmutableSet.of(source);
            }
        }
        return tags;
    }
    
    private static String serializeTags(Set<String> tags){
        String ser = tags.stream().reduce((c1,c2) -> c1.toLowerCase(Locale.ROOT)+":"+c2.toLowerCase(Locale.ROOT)).orElse("");
        if(!ser.contains(":")) return ser;
        return ser.substring(0,ser.lastIndexOf(":"));
    }
    
    public static void addItemTag(ItemStack item,String tag){
        if(item != null && item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            Set<String> tags = new HashSet<>(getItemTags(item));
            tags.add(tag);
            meta.getPersistentDataContainer().set(getItemTagsKey(),PersistentDataType.STRING,serializeTags(tags));
            item.setItemMeta(meta);
        }
    }
    
    public static void removeItemTag(ItemStack item, String tag){
        if(item != null && item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            Set<String> tags = new HashSet<>(getItemTags(item));
            tags.remove(tag);
            meta.getPersistentDataContainer().set(getItemTagsKey(),PersistentDataType.STRING,serializeTags(tags));
            item.setItemMeta(meta);
        }
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
    
    public static String getItemHelpText(ItemStack item){
        if(item != null && item.getItemMeta() != null){
            return Objects.requireNonNullElse(item.getItemMeta().getPersistentDataContainer().get(getHelpTextKey(),PersistentDataType.STRING),DEFAULT_HELP_TEXT);
        }
        return DEFAULT_HELP_TEXT;
    }
    
    public String getHelpText(){
        return this.helpText;
    }
    
    public void setHelpText(String helpText){
        this.helpText = helpText;
    }
    
    public static NamespacedKey getHelpTextKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"help-text");
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
