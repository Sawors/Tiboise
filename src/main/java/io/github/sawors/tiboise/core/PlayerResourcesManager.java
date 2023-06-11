package io.github.sawors.tiboise.core;

import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpServer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.integrations.voicechat.VoiceChatIntegrationPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class PlayerResourcesManager implements Listener {
    
    
    private static HashSet<UUID> reloadingPlayers = new HashSet<>();
    private static String packhash = null;
    // server-side resource management
    private static File resourceDirectory = null;
    private static File webServerDirectory = null;
    // local packed resource names
    private static final String packFileName = "resourcepack.zip";
    private static final String hashFileName = "sha1.txt";
    // webserver
    private static String webServerSrc;
    private static int webServerPort;
    private static final String resourcePackContext = "/download/TiboiseResourcePack.zip";
    private static final String resourcePackHashContext = "/download/sha1.txt";
    
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
    
    
    @EventHandler
    public static void checkResourcesOnJoin(PlayerJoinEvent event){
        final Player p = event.getPlayer();
        final boolean isTestMode = Tiboise.isServerInTestMode();
        if(p.isOp() && isTestMode){
            p.sendMessage(
                    Component.text("The server is in test mode ! This is not intended for production !")
                            .append(Component.text("\nTo change mode, please do \"tadmin testmode\""))
                            .color(TextColor.color(0xFF7300))
                            .hoverEvent(Component.text("Click to change mode"))
                            .clickEvent(ClickEvent.runCommand("/tadmin testmode"))
            );
        }
        // manually excluding myself in order to work quicker
        if(!isTestMode){
            new BukkitRunnable(){
                @Override
                public void run() {
                    sendPlayerResourcePack(event.getPlayer());
                    
                    final VoicechatConnection co = VoiceChatIntegrationPlugin.getVoicechatServerApi().getConnectionOf(p.getUniqueId()) ;
                    if(co == null || co.isDisabled()){
                        p.sendMessage(Component.text(ChatColor.RED+"You do not seem to have the simple voice chat mod installed. Please click ")
                                .append(Component.text(ChatColor.GOLD+""+ChatColor.UNDERLINE+"HERE").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,"https://minhaskamal.github.io/DownGit/#/home?url=https:%2F%2Fgithub.com%2FSawors%2Fmc-dev-server%2Fblob%2Fmain%2Fmods%20tiboise%20updated.zip")))
                                .append(Component.text(ChatColor.RED+" in order to install the server recommended modpack, or "))
                                .append(Component.text(ChatColor.GOLD+"here").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,"https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat")))
                                .append(Component.text(ChatColor.RED+" if you want the mod alone."))
                        );
                    }
                    
                }
            }.runTaskLater(Tiboise.getPlugin(),20*3);
        }
        
    }
    
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
    
    @EventHandler
    public static void reloadPlayerResourcePack(PlayerResourcePackStatusEvent event){
        final Player p = event.getPlayer();
        if(event.getStatus().equals(PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) && !reloadingPlayers.contains(p.getUniqueId())){
            new BukkitRunnable(){
                @Override
                public void run() {
                    sendPlayerResourcePack(p);
                    reloadingPlayers.add(p.getUniqueId());
                }
            }.runTaskLater(Tiboise.getPlugin(),20);
        } else if(event.getStatus().equals(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)){
            reloadingPlayers.remove(p.getUniqueId());
        }
    }
    
    public static void reloadPackData(){
        /*try(InputStream in = new URL(hashfile).openStream(); Scanner hashread = new Scanner(in)){
            packhash = hashread.next();
        }catch (IOException e){
            logAdmin("Can't load resource pack, malformed hash file URL");
        }*/
    }
    
    public static void sendPlayerResourcePack(Player p){
        p.setResourcePack(getPackSource(),getPackHashSource());
    }
    
    public static String getPackHash(){
        return packhash;
    }
    
    public static String getPackSource(){
        return webServerSrc+":"+webServerPort+resourcePackContext;
    }
    
    public static String getPackHashSource(){
        return webServerSrc+":"+webServerPort+resourcePackHashContext;
    }
    
    @EventHandler
    public static void initialize(PluginEnableEvent event){
        //webserver-ip: ""
        //webserver-port: 8123
        
        YamlConfiguration config = Tiboise.getConfigData();
        webServerSrc = config.getString("webserver-ip","mc.sawors.com");
        webServerPort = config.getInt("webserver-port",8123);
        
        
        
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            try{
                resourceDirectory = new File(Tiboise.getPlugin().getDataFolder().getPath()+File.separator+"resources");
                resourceDirectory.mkdirs();
                
                webServerDirectory = new File(resourceDirectory.getPath()+File.separator+ "webserver");
                webServerDirectory.mkdirs();
                
                File resourcePackBundled = new File(webServerDirectory.getPath()+File.separator+packFileName);
                File resourcePackBundledHash = new File(webServerDirectory.getPath()+File.separator+hashFileName);
                
                if(resourcePackBundled.exists()){
                    logAdmin("exists");
                    HttpServer server = HttpServer.create(new InetSocketAddress(webServerPort),8);
                    // context for downloading the resource pack
                    server.createContext(resourcePackContext, exchange -> {
                        try(OutputStream out = exchange.getResponseBody(); InputStream in = new FileInputStream(resourcePackBundled)) {
                            exchange.sendResponseHeaders(200, resourcePackBundled.length());
                            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename="+packFileName);
                            exchange.setAttribute(HttpHeaders.CONTENT_TYPE, "application/zip");
                            out.write(in.readAllBytes());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
                    // context for downloading the sha1 text file
                    server.createContext(resourcePackHashContext, exchange -> {
                        try(OutputStream out = exchange.getResponseBody(); InputStream in = new FileInputStream(resourcePackBundledHash)) {
                            exchange.sendResponseHeaders(200, resourcePackBundledHash.length());
                            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename="+hashFileName);
                            exchange.setAttribute(HttpHeaders.CONTENT_TYPE, "text/plain");
                            out.write(in.readAllBytes());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
                    server.start();
                }
                /*final String fileToCopy = "index.html";
                try(InputStream in = PlayerResourcesManager.class.getResourceAsStream("io/github/tiboise/resources/webserver/"+fileToCopy)){
                    
                    File out = new File(webServerDirectory.getPath()+File.separator+"index.html");
                    out.createNewFile();
                    
                    if(in != null && out.exists()){
                        Files.copy(in, out.toPath());
                    }
                    
                    
                    
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }*/
                // TODO : Implement server-side resourcepack management
            } catch (SecurityException e){
                e.printStackTrace();
            } catch (
                    IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    static void zipResourcePack(){
        if(resourceDirectory != null && resourceDirectory.exists()){
            File resourcePackBundled = new File(webServerDirectory.getPath()+File.separator+"resourcepack.zip");
            File resourcePackBundledHash = new File(webServerDirectory.getPath()+File.separator+"sha1.txt");
            
            
        }
    }
    
    static void downloadBaseResourcePack(){
    
    }
}
