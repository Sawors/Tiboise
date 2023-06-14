package io.github.sawors.tiboise;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import io.github.sawors.tiboise.agriculture.AnimalsManager;
import io.github.sawors.tiboise.agriculture.CropsManager;
import io.github.sawors.tiboise.core.*;
import io.github.sawors.tiboise.core.commands.*;
import io.github.sawors.tiboise.core.local.DataPackManager;
import io.github.sawors.tiboise.core.local.LocalResourcesManager;
import io.github.sawors.tiboise.core.local.ResourcePackManager;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.economy.trade.TradingStation;
import io.github.sawors.tiboise.exploration.ExplorationGeneralFeatures;
import io.github.sawors.tiboise.integrations.bungee.BungeeListener;
import io.github.sawors.tiboise.integrations.bungee.KidnapCommand;
import io.github.sawors.tiboise.integrations.voicechat.VoiceChatIntegrationPlugin;
import io.github.sawors.tiboise.items.GiveItemCommand;
import io.github.sawors.tiboise.items.ItemGlobalListeners;
import io.github.sawors.tiboise.items.TiboiseItem;
import io.github.sawors.tiboise.items.discs.DiscCommand;
import io.github.sawors.tiboise.items.discs.MusicManager;
import io.github.sawors.tiboise.items.utility.coppercompass.PlayerCompassMarker;
import io.github.sawors.tiboise.items.utility.coppercompass.PlayerMarkerCommand;
import io.github.sawors.tiboise.post.LetterCommand;
import io.github.sawors.tiboise.post.PostLetterBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;

public final class Tiboise extends JavaPlugin {
    private static File configfile = null;
    private static JavaPlugin instance = null;
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
    // versions and patchnote
    private static final String version = "1.2";
    // discord
    //private static JDA jdaInstance = null;
    // resource pack
    private static File resourceDirectory = null;
    //
    private static File ffmpegInstallation = null;
    //
    private static File ytdlpInstallation = null;

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
        
        // DISCORD BOT
        if(configfile != null){
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configfile);
//            final String token = configuration.getString("token");
//            if(token != null){
//                jdaInstance = JDABuilder.createDefault(token)
//                        .enableIntents(Set.of(
//                                GatewayIntent.DIRECT_MESSAGES
//                        ))
//                        .setActivity(Activity.playing("Tiboise 1.19.4"))
//                .build();
//                logAdmin("Discord bot enabled !");
//            } else {
//                Bukkit.getLogger().log(Level.WARNING,"[Tiboise] Discord features disabled, please add your bot token to config.yml in the field token:\"\"");
//            }
            
            // FFMPEG
            final String pathToFfmpeg = configuration.getString("path-to-ffmpeg");
            if(pathToFfmpeg != null && pathToFfmpeg.length() > 1){
                ffmpegInstallation = new File(pathToFfmpeg);
                try{
                    if(
                            new HashSet<>(Arrays.asList(Objects.requireNonNull(ffmpegInstallation.list()))).stream().noneMatch(s -> s.startsWith("ffmpeg"))
                            || new HashSet<>(Arrays.asList(Objects.requireNonNull(ffmpegInstallation.list()))).stream().noneMatch(s -> s.startsWith("ffprobe"))
                    ){
                        ffmpegInstallation = null;
                    }
                } catch (NullPointerException e){
                    ffmpegInstallation = null;
                }
            }
            // YTDLP
            final String pathToYtdlp = configuration.getString("path-to-ytdlp");
            logAdmin("path",pathToYtdlp);
            if(pathToYtdlp != null && pathToYtdlp.length() > 1){
                ytdlpInstallation = new File(pathToYtdlp);
                try{
                    logAdmin(new HashSet<>(Arrays.asList(Objects.requireNonNull(ytdlpInstallation.list()))));
                    if(
                            new HashSet<>(Arrays.asList(Objects.requireNonNull(ytdlpInstallation.list()))).stream().noneMatch(s -> s.startsWith("yt-dlp"))
                    ){
                        logAdmin("fail");
                        ytdlpInstallation = null;
                    }
                } catch (NullPointerException e){
                    ytdlpInstallation = null;
                }
            }
        }
        
        TiboiseUtils.initialize();
        /*dbfile = new File(getPlugin().getDataFolder()+File.separator+"database.db");
        try{
            Tiboise.logAdmin("Database located at "+dbfile);
            dbfile.createNewFile();
        } catch (
                IOException e){
            e.printStackTrace();
            Tiboise.logAdmin("database creation failed, could not access the file");
        }
        DatabaseLink.connectInit();*/
        
        final Server server = getServer();
        final PluginManager manager = server.getPluginManager();
        manager.registerEvents(new ItemGlobalListeners(), this);
        manager.registerEvents(new SpawnManager(),this);
        manager.registerEvents(new QOLImprovements(),this);
        manager.registerEvents(new CropsManager(),this);
        manager.registerEvents(new AnimalsManager(), this);
        manager.registerEvents(new LocalResourcesManager(), this);
        manager.registerEvents(new ResourcePackManager(), this);
        manager.registerEvents(new DataPackManager(), this);
        manager.registerEvents(new PlayerCompassMarker(), this);
        manager.registerEvents(new ExplorationGeneralFeatures(), this);
        manager.registerEvents(new OfflinePlayerManagement(), this);
        manager.registerEvents(new FloatingTextUtils(), this);
        manager.registerEvents(new CraftingPatcher(),this);
        manager.registerEvents(new PostLetterBox(),this);
        manager.registerEvents(new SittingManager(),this);
        manager.registerEvents(new MusicManager(), this);
        manager.registerEvents(new VillagerManager(), this);
        manager.registerEvents(new TradingStation(null,null), this);
        manager.registerEvents(new OwnedBlock() {@Override public UUID getOwner() {return null;}}, this);
        
        Objects.requireNonNull(server.getPluginCommand("tgive")).setExecutor(new GiveItemCommand());
        Objects.requireNonNull(server.getPluginCommand("tid")).setExecutor(new GetIdCommand());
        Objects.requireNonNull(server.getPluginCommand("ttest")).setExecutor(new TTestCommand());
        Objects.requireNonNull(server.getPluginCommand("kidnap")).setExecutor(new KidnapCommand());
        Objects.requireNonNull(server.getPluginCommand("marker")).setExecutor(new PlayerMarkerCommand());
        Objects.requireNonNull(server.getPluginCommand("tadmin")).setExecutor(new TAdminCommand());
        Objects.requireNonNull(server.getPluginCommand("thelp")).setExecutor(new THelpCommand());
        Objects.requireNonNull(server.getPluginCommand("letter")).setExecutor(new LetterCommand());
        Objects.requireNonNull(server.getPluginCommand("sit")).setExecutor(new SitCommand());
        Objects.requireNonNull(server.getPluginCommand("mp")).setExecutor(new AdminMessageCommand());
        Objects.requireNonNull(server.getPluginCommand("pack")).setExecutor(new PackCommand());
        Objects.requireNonNull(server.getPluginCommand("disc")).setExecutor(new DiscCommand());
        TiboiseMainCommand maincommand = new TiboiseMainCommand();
        Objects.requireNonNull(server.getPluginCommand("tiboise")).setExecutor(maincommand);
        Objects.requireNonNull(server.getPluginCommand("tiboise")).setTabCompleter(maincommand);

        
        
    
        // made so player nametags disappear, TODO add a config option to control this
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        t = scoreboard.getTeam("hide_name");
        if(t == null){
            t = scoreboard.registerNewTeam("hide_name");
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            t.setCanSeeFriendlyInvisibles(false);
        }
       
        
        for(World w : Bukkit.getWorlds()){
            FloatingTextUtils.cleanupTempDisplays(w);
        }
        
        

        if(economy){
            CoinItem.loadCoinValues();
        }

        if(fishing){
//            FishingManager.loadFishVariants();
//            FishingManager.loadLegendaryFishVariants();
//            FishingManager.loadWaterZones();
        }
        
        // ITEM REGISTRATION :
        TiboiseItem.loadItems();
        
        CropsManager.loadBonemealList();
        
        /*protocolManager.addPacketListener(new PacketAdapter(
                this, ListenerPriority.LOWEST,
                PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket().deepClone();
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    ItemStack content = packet.getItemModifier().readSafely(0);
                    logAdmin(TiboiseItem.getItemTags(content));
                    if(content != null && TiboiseItem.getItemTags(content).contains(ItemTag.HIDE_FROM_CLIENT.toString())) {
                        event.setCancelled(true);
                        ItemMeta meta = content.getItemMeta();
                        //event.setCancelled(true);
                        for(Enchantment ench : meta.getEnchants().keySet()){
                            meta.removeEnchant(ench);
                        }
                        content.setItemMeta(meta);
                        packet.getItemModifier().writeSafely(0,content);
                        logAdmin("TRUE - SINGLE");
                    }
                } else {
                    //ItemStack[] content = event.getPacket().getItemArrayModifier().readSafely(0);
                    List<ItemStack> array = packet.getItemListModifier().readSafely(0);
                    logAdmin("0",packet.getItemArrayModifier().readSafely(0));
                    logAdmin("1",packet.getItemArrayModifier().readSafely(1));
                    logAdmin("2",packet.getItemArrayModifier().readSafely(2));
                    logAdmin("3",packet.getItemArrayModifier().readSafely(3));
                    logAdmin("1.0",packet.getItemListModifier().readSafely(0));
                    logAdmin("1.1",packet.getItemListModifier().readSafely(1));
                    logAdmin("1.2",packet.getItemListModifier().readSafely(2));
                    logAdmin("1.3",packet.getItemListModifier().readSafely(3));
                    if(array != null){
                        for(ItemStack content : array){
                            logAdmin(TiboiseItem.getItemTags(content));
                            if(content != null && TiboiseItem.getItemTags(content).contains(ItemTag.HIDE_FROM_CLIENT.toString())) {
                                //event.setCancelled(true);
                                array.remove(content);
                                logAdmin("HIDE");
                                ItemMeta meta = content.getItemMeta();
                                //event.setCancelled(true);
                                for(Enchantment ench : meta.getEnchants().keySet()){
                                    meta.removeEnchant(ench);
                                }
                                content.setItemMeta(meta);
                                logAdmin("TRUE - ARRAY");
                            }
                        }
                    }
                    event.getPacket().getItemListModifier().writeSafely(0,array);
                }
                event.setPacket(packet);
            }
        });*/
        
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (vcplugin != null) {
            getServer().getServicesManager().unregister(vcplugin);
        }
    }

    public static YamlConfiguration getConfigData() {
        return YamlConfiguration.loadConfiguration(configfile);
    }

    public static JavaPlugin getPlugin(){
        return instance;
    }
    
    public static String getVersion(){
        return version;
    }
    
//    static JDA getJdaInstance(){
//        return jdaInstance;
//    }
    
    
    
    public static @Nullable File getFFmpegInstallation() {
        return ffmpegInstallation;
    }
    
    public static File getYtdlpInstallation() {
        return ytdlpInstallation;
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
        if(sec != null) {
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

    public static void logAdmin(Object msg) throws IllegalStateException{
        logAdmin(null,msg);
    }
    public static void logAdmin(@Nullable Object title, Object msg) throws IllegalStateException{
        if(isServerInTestMode()){
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
        } else {
            throw new IllegalStateException("[Tiboise "+version+"]"+" Test logging used in production environment, please warn the devs !");
        }
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
    
    public static void setTestMode(boolean testMode){
        testmode = testMode;
        try{
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configfile);
            config.set(ConfigFields.TEST_MODE.getFieldName(),testMode);
            config.save(configfile);
        } catch (IllegalArgumentException | IOException e){
            e.printStackTrace();
        }
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
    
    public enum ConfigModules {
        FISHING, BETTERVANILLA, PAINTING, ECONOMY;
        
        
        public String getName(){
            return this.toString().toLowerCase(Locale.ROOT);
        }
    }
    
    public enum ConfigFields{
        TEST_MODE("test-mode"),
        ;
        
        private final String fieldName;
        ConfigFields(String name){
            this.fieldName = name;
        }
        
        public String getFieldName(){
            return fieldName;
        }
    }
    
}
