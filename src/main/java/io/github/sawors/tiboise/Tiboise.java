package io.github.sawors.tiboise;

import io.github.sawors.tiboise.core.commands.GetIdCommand;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.fishing.FishingManager;
import io.github.sawors.tiboise.items.GiveItemCommand;
import io.github.sawors.tiboise.items.ItemGlobalListeners;
import io.github.sawors.tiboise.items.MagicStick;
import io.github.sawors.tiboise.items.TiboiseItem;
import io.github.sawors.tiboise.painting.PaintingHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;

public final class Tiboise extends JavaPlugin {

    private static File configfile = null;
    private static JavaPlugin instance = null;
    private static HashMap<String, TiboiseItem> itemmap = new HashMap<>();
    private static HashSet<Integer> registeredlisteners = new HashSet<>();
    // modules
    private static List<String> enabledmodules = new ArrayList<>();
    private static boolean bettervanilla = true;
    private static boolean fishing = true;
    private static boolean painting = true;
    private static boolean economy = true;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        configfile = new File(getPlugin().getDataFolder()+ File.separator+"config.yml");

        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PaintingHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemGlobalListeners(), this);
        
        Objects.requireNonNull(getServer().getPluginCommand("tgive")).setExecutor(new GiveItemCommand());
        Objects.requireNonNull(getServer().getPluginCommand("tid")).setExecutor(new GetIdCommand());

        registerItem(new MagicStick());
        registerItem(new CoinItem());


        loadConfigOptions();

        if(economy){
            CoinItem.loadCoinValues();
        }

        if(fishing){


            FishingManager.loadFishVariants();
            FishingManager.loadLegendaryFishVariants();
            FishingManager.loadWaterZones();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public static JavaPlugin getPlugin(){
        return instance;
    }

    public static boolean isModuleEnabled(ConfigModules module){
        switch(module){
            case FISHING -> {return fishing;}
            case ECONOMY -> {return economy;}
            case PAINTING -> {return painting;}
            case BETTERVANILLA -> {return bettervanilla;}
            default -> {return true;}
        }
    }

    public static @Nullable ConfigurationSection getModuleSection(ConfigModules module){
        YamlConfiguration configdata = YamlConfiguration.loadConfiguration(configfile);
        ConfigurationSection sec = configdata.getConfigurationSection("modules");
        if(sec != null){
            return sec.getConfigurationSection(module.getName());
        }

        return null;
    }

    private static void loadConfigOptions(){
        YamlConfiguration configdata = YamlConfiguration.loadConfiguration(configfile);
        ConfigurationSection modules = configdata.getConfigurationSection("modules");
        if(modules != null){
            for(String module : modules.getKeys(false)){
                ConfigurationSection modsec = modules.getConfigurationSection(module);
                if(modsec != null){
                    boolean enabled = modsec.getBoolean("enabled");
                    switch (modsec.getName()){
                        case "bettervanilla" -> {bettervanilla = enabled;}
                        case "paiting" -> {painting = enabled;}
                        case "economy" -> {economy = enabled;}
                        case "fishing" -> {fishing = enabled;}
                    }
                    if(enabled){
                        enabledmodules.add(modsec.getName());
                        Bukkit.getLogger().log(Level.INFO,ChatColor.GOLD+"[TIBOISE] module ["+modsec.getName()+"] enabled !");
                    }
                }
            }
        }
    }

    public static void logAdmin(Object msg){
        logAdmin(null,msg);
    }
    public static void logAdmin(@Nullable Object title, Object msg){
        String pluginname = getPlugin().getName();
        String inter = "";
        if(title != null && title.toString().length() > 0){
            inter = title+" : ";
        }

        String output = "["+ ChatColor.YELLOW+pluginname+" DEBUG"+ChatColor.WHITE+"-"+ Time.valueOf(LocalTime.now()) + "] "+inter+msg;
        Bukkit.getLogger().log(Level.INFO, output);
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.isOp()){
                p.sendMessage(Component.text(output));
            }
        }
    }

    private void registerItem(TiboiseItem item){
        itemmap.put(item.getId(), item);


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
                    logAdmin("Listener found : "+method.getName());
                    Class<? extends Event> itemclass = method.getParameters()[0].getType().asSubclass(Event.class);

                    getServer().getPluginManager().registerEvent(itemclass, listener, method.getAnnotation(EventHandler.class).priority(), EventExecutor.create(method, itemclass),getPlugin());
                    registeredlisteners.add(method.hashCode());
                }
            }
        }
    }

    public static TiboiseItem getRegisteredItem(String id){
        return itemmap.get(id);
    }

    public static Inventory getItemListDisplay(){
        Inventory itemsview = Bukkit.createInventory(null, 6*9, Component.text("Item List"));
        for(TiboiseItem item : itemmap.values()){
            itemsview.addItem(item.get());
        }

        return itemsview;
    }


    public static String getComponentContent(Component component){
        StringBuilder out = new StringBuilder();
        String in = component.toString();
        String ref = "no ref";
        char[] content = in.toCharArray();
        if(component instanceof TranslatableComponent){
            ref = "\"translate\":\"";
        } else if(component instanceof TextComponent){
            ref = "\"extra\":\"";
        }
    
        int base = in.indexOf(ref);
        if(base > -1){
            for(int i = base+ref.length(); i<content.length+4; i++){
                char c = content[i];
                if(c != '\"'){
                    out.append(content[i]);
                } else {
                    break;
                }
            }
        }
        
        return out.toString();
    }
    
    public static boolean isCraftingInventory(Inventory inv) {
        InventoryType type = inv.getType();
        return
                !(type.equals(InventoryType.CHEST)
                        || type.equals(InventoryType.BARREL)
                        || type.equals(InventoryType.ANVIL)
                        || type.equals(InventoryType.BEACON)
                        || type.equals(InventoryType.CREATIVE)
                        || type.equals(InventoryType.DISPENSER)
                        || type.equals(InventoryType.DROPPER)
                        || type.equals(InventoryType.ENCHANTING)
                        || type.equals(InventoryType.ENDER_CHEST)
                        || type.equals(InventoryType.HOPPER)
                        || type.equals(InventoryType.LECTERN)
                        || type.equals(InventoryType.PLAYER)
                        || type.equals(InventoryType.SHULKER_BOX));
    }
}
