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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class PlayerResourcesManager implements Listener {
    
    
    private static HashSet<UUID> reloadingPlayers = new HashSet<>();
    private static String packhash = null;
    // server-side resource management
    private static File resourceDirectory = null;
    private static File webServerDirectory = null;
    // local packed resource names
    private static final String packFileName = "TiboiseResourcePack.zip";
    private static final String hashFileName = "sha1.txt";
    private final static String packSource = "https://github.com/Sawors/Tiboise/raw/master/src/main/resources/resourcepack/Tiboise-1.19.2.zip";
    private static File packSourceFile;
    // webserver
    private static String webServerSrc;
    private static int webServerPort;
    private static final String resourcePackContext = "/download/"+packFileName;
    private static final String resourcePackHashContext = "/download/"+hashFileName;
    
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
        logAdmin(event.getStatus());
        logAdmin(event.getHash());
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
    
    public static void rebuildResourcePack(){
        logAdmin(packSourceFile);
        logAdmin(packSourceFile != null && packSourceFile.exists());
        try (BufferedInputStream in = new BufferedInputStream(new URL(packSource).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(packSourceFile)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
        }
        if(packSourceFile != null && packSourceFile.exists()){
            // decompress pack source
            try(ZipInputStream zis = new ZipInputStream(new FileInputStream(packSourceFile));){
                byte[] buffer = new byte[1024];
                
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = new File(packSourceFile.getParentFile().getPath()+File.separator+"temp");
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        
                        // write file content
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    zipEntry = zis.getNextEntry();
                }
                
                zis.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void sendPlayerResourcePack(Player p){
        try{
            String hash = Files.readString(Path.of(webServerDirectory + File.separator + hashFileName));
            hash = hash.substring(0,Math.min(40,hash.length()));
            p.setResourcePack(getPackSource(), hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            
            YamlConfiguration config = Tiboise.getConfigData();
            webServerSrc = config.getString("webserver-ip","http://mc.sawors.com");
            if(!webServerSrc.startsWith("http")){
                webServerSrc = "http://"+webServerSrc;
            }
            webServerPort = config.getInt("webserver-port",8123);
            
            try{
                resourceDirectory = new File(Tiboise.getPlugin().getDataFolder().getPath()+File.separator+"resources");
                resourceDirectory.mkdirs();
                
                webServerDirectory = new File(resourceDirectory.getPath()+File.separator+ "webserver");
                webServerDirectory.mkdirs();
                
                
                String parent = resourceDirectory.getPath()+File.separator+"resourcepack";
                new File(parent).mkdirs();
                packSourceFile = new File(parent+File.separator+"source.zip");
                try{
                    packSourceFile.createNewFile();
                } catch (IOException e){
                    e.printStackTrace();
                }
                logAdmin(packSourceFile.getPath());
                rebuildResourcePack();
                
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
