package io.github.sawors.tiboise.core.local;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.discs.MusicDisc;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.github.sawors.tiboise.Tiboise.logAdmin;


public class LocalResourcesManager implements Listener {
    
    // server-side resource management
    private static File resourceDirectory = null;
    private static File webServerDirectory = null;
    private static File musicStorageDirectory;
    private static File localResourcePackDirectory;
    //
    private static String webServerSrc;
    private static int webServerPort;
    //
    private static File packSourceFile;
    // local assets
    private static File assets;
    private static File musicIndexFile;
    
    
    final private static Map<String, UUID> nickNamesMap = new HashMap<>(Map.of(
            "GrosOrteilDePied",UUID.fromString("66e25a14-b468-4cb1-8cde-6cf6054255ba"),
            "MOLE1283", UUID.fromString("30b80f6f-f0dc-4b4a-96b2-c37b28494b1b"),
            "WalidBedouin", UUID.fromString("6864eb4a-91d6-4292-8dfb-f398cbd5dc57")
    ));
    final private static Map<String, String> realNamesMap = new HashMap<>(Map.of(
            "GrosOrteilDePied","DiggyDiggyMole",
            "MOLE1283", "DimitriScgnt",
            "WalidBedouin", "esprit-absent"
    ));
    
    
    public static void setNickName(UUID player, String nickname){
        if(nickNamesMap.containsKey(nickname.replaceAll(" ",""))){
            throw new IllegalArgumentException("a player already has this nickname");
        }
        nickNamesMap.put(nickname.replaceAll(" ",""),player);
    }
    
    public static boolean hasNickName(UUID player){
        return nickNamesMap.containsValue(player);
    }
    
    public static String getNickName(UUID player){
        Optional<Map.Entry<String,UUID>> pid = nickNamesMap.entrySet().stream().filter(p -> p.getValue().equals(player)).findFirst();
        return pid.map(Map.Entry::getKey).orElse(null);
    }
    
    public static UUID getNickNamedPlayer(String nickname){
        return nickNamesMap.get(nickname);
    }
    
    public static String getRealName(String playerName){
        if(playerName == null) return null;
        return realNamesMap.getOrDefault(playerName,playerName);
    }
    
    public static File getResourceDirectory() {
        return resourceDirectory;
    }
    
    public static File getMusicStorageDirectory() {
        return musicStorageDirectory;
    }
    
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public static void initialize(PluginEnableEvent event){
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            
            YamlConfiguration config = Tiboise.getConfigData();
            webServerSrc = config.getString("webserver-ip","http://mc.sawors.com");
            if(!webServerSrc.startsWith("http")){
                webServerSrc = "http://"+webServerSrc;
            }
            
            webServerPort = config.getInt("webserver-port",8123);
            logAdmin(webServerPort);
            try{
                resourceDirectory = new File(Tiboise.getPlugin().getDataFolder().getPath()+File.separator+"resources");
                resourceDirectory.mkdirs();
                
                webServerDirectory = new File(resourceDirectory.getPath()+File.separator+ "webserver");
                webServerDirectory.mkdirs();
                
                
                localResourcePackDirectory = new File(resourceDirectory.getPath()+File.separator+"resourcepack");
                localResourcePackDirectory.mkdirs();
                packSourceFile = new File(localResourcePackDirectory.getPath()+File.separator+"source.zip");
                
                assets = new File(localResourcePackDirectory.getPath()+File.separator+"assets");
                assets.mkdirs();
                
                
                if(musicStorageDirectory == null || !musicStorageDirectory.exists()){
                    musicStorageDirectory = new File(getResourceDirectory().getPath()+File.separator+"musics");
                    musicStorageDirectory.mkdirs();
                }
                musicIndexFile = new File(getMusicStorageDirectory().getPath()+File.separator+"music_index.yml");
                if(!musicIndexFile.exists()){
                    try{
                        musicIndexFile.createNewFile();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                
                try{
                    packSourceFile.createNewFile();
                } catch (IOException e){
                    e.printStackTrace();
                }
                
                MusicDisc.loadMusicIndex();
                
                ResourcePackManager.initialize();
                DataPackManager.initialize();
                WebServerManager.initialize();
            } catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }
    
    protected static void setResourceDirectory(File resourceDirectory) {
        LocalResourcesManager.resourceDirectory = resourceDirectory;
    }
    
    public static File getWebServerDirectory() {
        return webServerDirectory;
    }
    
    protected static void setWebServerDirectory(File webServerDirectory) {
        LocalResourcesManager.webServerDirectory = webServerDirectory;
    }
    
    protected static void setMusicStorageDirectory(File musicStorageDirectory) {
        LocalResourcesManager.musicStorageDirectory = musicStorageDirectory;
    }
    
    public static File getLocalResourcePackDirectory() {
        return localResourcePackDirectory;
    }
    
    protected static void setLocalResourcePackDirectory(File localResourcePackDirectory) {
        LocalResourcesManager.localResourcePackDirectory = localResourcePackDirectory;
    }
    
    protected static String getWebServerSrc() {
        return webServerSrc;
    }
    
    protected static int getWebServerPort() {
        return webServerPort;
    }
    
    public static File getMusicIndexFile() {
        return musicIndexFile;
    }
    
    public static File getAssetDirectory(){
        return assets;
    }
    
    public static File getPackSourceFile() {
        return packSourceFile;
    }
    
}
