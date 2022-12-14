package io.github.sawors.tiboise;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import io.github.sawors.tiboise.agriculture.AnimalsManager;
import io.github.sawors.tiboise.agriculture.CropsManager;
import io.github.sawors.tiboise.core.*;
import io.github.sawors.tiboise.core.commands.GetIdCommand;
import io.github.sawors.tiboise.core.commands.TTestCommand;
import io.github.sawors.tiboise.core.commands.TiboiseMainCommand;
import io.github.sawors.tiboise.core.database.DatabaseLink;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.exploration.ExplorationGeneralFeatures;
import io.github.sawors.tiboise.exploration.PlayerCompassMarker;
import io.github.sawors.tiboise.exploration.PlayerMarkerCommand;
import io.github.sawors.tiboise.exploration.items.CopperCompass;
import io.github.sawors.tiboise.integrations.bungee.BungeeListener;
import io.github.sawors.tiboise.integrations.bungee.KidnapCommand;
import io.github.sawors.tiboise.integrations.voicechat.PortableRadio;
import io.github.sawors.tiboise.integrations.voicechat.VoiceChatIntegrationPlugin;
import io.github.sawors.tiboise.items.*;
import io.github.sawors.tiboise.items.tools.radius.Excavator;
import io.github.sawors.tiboise.items.tools.radius.Hammer;
import io.github.sawors.tiboise.items.tools.tree.Broadaxe;
import io.github.sawors.tiboise.painting.PaintingHandler;
import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
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
    private static boolean testmode = false;
    private static Team t;
    // modules
    private static List<String> enabledmodules = new ArrayList<>();
    private static boolean bettervanilla = true;
    private static boolean fishing = true;
    private static boolean painting = true;
    private static boolean economy = true;
    // integrations
    private static boolean vcenabled = false;
    private static VoicechatPlugin vcplugin = null;
    private static boolean usebungee = false;
    private static ProtocolManager protocolManager;
    // database
    private static File dbfile;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        configfile = new File(getPlugin().getDataFolder()+ File.separator+"config.yml");

        this.saveDefaultConfig();
        loadConfigOptions();
        BukkitVoicechatService vcservice = getServer().getServicesManager().load(BukkitVoicechatService.class);
        vcenabled = vcservice != null;
        if(vcenabled){
            vcplugin = new VoiceChatIntegrationPlugin();
            vcservice.registerPlugin(vcplugin);
            logAdmin("Simple Voice Chat plugin detected, integration enabled");
        }
    
        try{
            getServer().getMessenger().registerOutgoingPluginChannel(getPlugin(), getBungeeChannel());
            getServer().getMessenger().registerIncomingPluginChannel(this, getBungeeChannel(), new BungeeListener());
            usebungee = true;
        } catch (IllegalArgumentException e){
            logAdmin("BungeeCord features disables, BungeeCord not found");
            usebungee = false;
        }
    
        protocolManager = ProtocolLibrary.getProtocolManager();
        
        //getServer().getMessenger().registerOutgoingPluginChannel(getPlugin(), getVanillaRebootChannel());
        //getServer().getMessenger().registerIncomingPluginChannel(this, getVanillaRebootChannel(), new BungeeListener());
    
        logAdmin( "BungeeCord features enabled !");
    
        dbfile = new File(getPlugin().getDataFolder()+File.separator+"database.db");
        try{
            Tiboise.logAdmin("Database located at "+dbfile);
            dbfile.createNewFile();
        } catch (
                IOException e){
            e.printStackTrace();
            Tiboise.logAdmin("database creation failed, could not access the file");
        }
        DatabaseLink.connectInit();
        
        ResourcePackManager.reloadPackData();
        
        getServer().getPluginManager().registerEvents(new PaintingHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemGlobalListeners(), this);
        getServer().getPluginManager().registerEvents(new SpawnManager(),this);
        getServer().getPluginManager().registerEvents(new QOLImprovements(),this);
        getServer().getPluginManager().registerEvents(new CropsManager(),this);
        getServer().getPluginManager().registerEvents(new AnimalsManager(), this);
        getServer().getPluginManager().registerEvents(new ResourcePackManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerCompassMarker(), this);
        getServer().getPluginManager().registerEvents(new ExplorationGeneralFeatures(), this);
        getServer().getPluginManager().registerEvents(new OfflinePlayerManagement(), this);
        
        Objects.requireNonNull(getServer().getPluginCommand("tgive")).setExecutor(new GiveItemCommand());
        Objects.requireNonNull(getServer().getPluginCommand("tid")).setExecutor(new GetIdCommand());
        Objects.requireNonNull(getServer().getPluginCommand("ttest")).setExecutor(new TTestCommand());
        Objects.requireNonNull(getServer().getPluginCommand("kidnap")).setExecutor(new KidnapCommand());
        Objects.requireNonNull(getServer().getPluginCommand("marker")).setExecutor(new PlayerMarkerCommand());
        TiboiseMainCommand maincommand = new TiboiseMainCommand();
        Objects.requireNonNull(getServer().getPluginCommand("tiboise")).setExecutor(maincommand);
        Objects.requireNonNull(getServer().getPluginCommand("tiboise")).setTabCompleter(maincommand);

        registerItem(new MagicStick());
        registerItem(new Hammer());
        registerItem(new Excavator());
        registerItem(new Broadaxe());
        registerItem(new CopperCompass());
        registerItem(new TiboiseWrench());
        if(isModuleEnabled(ConfigModules.ECONOMY)){
            registerItem(new CoinItem());
        }
        if(isVoiceChatEnabled()){
            registerItem(new PortableRadio());
        }
    
        // made so player nametags disappear, TODO add a config option to control this
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        t = scoreboard.getTeam("hide_name");
        if(t == null){
            t = scoreboard.registerNewTeam("hide_name");
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            t.setCanSeeFriendlyInvisibles(false);
        }
       
        
    
        
        

        if(economy){
            CoinItem.loadCoinValues();
        }

        if(fishing){
//            FishingManager.loadFishVariants();
//            FishingManager.loadLegendaryFishVariants();
//            FishingManager.loadWaterZones();
        }
        
        CropsManager.loadBonemealList();
        
        TiboiseUtils.initialize();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (vcplugin != null) {
            getServer().getServicesManager().unregister(vcplugin);
        }
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
    
    public static ProtocolManager getProtocolManager(){
        return protocolManager;
    }

    private static void loadConfigOptions(){
        YamlConfiguration configdata = YamlConfiguration.loadConfiguration(configfile);
        ConfigurationSection modules = configdata.getConfigurationSection("modules");
        testmode = configdata.getBoolean("test-mode");
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
        String ref = "content=\"";
        char[] content = in.toCharArray();
        if(component instanceof TranslatableComponent){
            ref = "translate=\"";
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
        
        if(out.length() <= 0){
            out = new StringBuilder();
            ref = "extra=\"";
            base = in.indexOf(ref);
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

    /**
     *
     * @param text The text to get the UpperCamelCase form from. Use spaces to determine word separation.
     * @return The text with the first letter of each word capitalized. For instance : "never dig straight down" returns "Never Dig Straight Down"
     */
    public static String getUpperCamelCase(String text){
        StringBuilder otp = new StringBuilder();
        String[] subs = text.split(" ");
        for(int i = 0; i<subs.length; i++){
            String s = subs[i];
            otp.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
            if(i<subs.length-1){
                otp.append(" ");
            }
        }
        
        return otp.toString();
    }
    
    public static boolean isVoiceChatEnabled(){
        return vcenabled;
    }
    
    public static boolean isServerInTestMode(){
        return testmode;
    }
    
    public static void addPlayerToInvisibleNametagTeam(Player p){
        t.addEntities(p);
    }
    
    public static File getDbFile(){
        return dbfile;
    }
    
    public static String getBungeeChannel(){
        return "BungeeCord";
    }
    
    public static String getVanillaRebootChannel(){
        return "VanillaReboot";
    }
    
    public static boolean isBungeeEnabled(){
        return usebungee;
    }
    
}
