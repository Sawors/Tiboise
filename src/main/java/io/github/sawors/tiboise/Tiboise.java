package io.github.sawors.tiboise;

import io.github.sawors.tiboise.items.MagicStick;
import io.github.sawors.tiboise.items.TiboiseItem;
import io.github.sawors.tiboise.painting.PaintingHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Level;

public final class Tiboise extends JavaPlugin {

    private static File configfile = null;
    private static JavaPlugin instance = null;
    private static HashMap<String, TiboiseItem> itemmap = new HashMap<>();
    private static HashSet<Integer> registeredlisteners = new HashSet<>();
    // modules
    private static boolean bettervanilla = true;
    private static boolean fishing = true;
    private static boolean painting = true;
    private static boolean economy = true;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        configfile = new File(getPlugin().getDataFolder()+"config.yml");

        this.saveDefaultConfig();

        registerItem(new MagicStick());
        getServer().getPluginManager().registerEvents(new PaintingHandler(), this);

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
            return sec.getConfigurationSection(module.toString().toLowerCase(Locale.ROOT));
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
                }
            }
        }
    }

    public static void logAdmin(Object msg){
        logAdmin(null,msg);
    }
    public static void logAdmin(@Nullable Object title, Object msg){

        String inter = "";
        if(title != null && title.toString().length() > 0){
            inter = title+" : ";
        }

        String output = "["+ ChatColor.YELLOW+"DEBUG"+ChatColor.WHITE+"-"+ Time.valueOf(LocalTime.now()) + "] "+inter+msg;
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


}
