package io.github.sawors.tiboise.core;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpServer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.integrations.voicechat.VoiceChatIntegrationPlugin;
import io.github.sawors.tiboise.items.discs.MusicDisc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static io.github.sawors.tiboise.Tiboise.logAdmin;


public class LocalResourcesManager implements Listener {
    
    
    private static HashSet<UUID> reloadingPlayers = new HashSet<>();
    private static String packhash = null;
    // server-side resource management
    private static File resourceDirectory = null;
    private static File webServerDirectory = null;
    // local packed resource names
    private static final String packFileName = "TiboiseResourcePack.zip";
    private static final String hashFileName = "sha1.txt";
    private final static String packSource = "https://github.com/Sawors/Tiboise/raw/master/resourcepack/Tiboise-1.19.2.zip";
    private static File localResourcePackDirectory;
    private static File packSourceFile;
    // local assets
    private static File assets;
    private static File musicStorageDirectory;
    private static File musicIndexFile;
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
    
    public static File getResourceDirectory() {
        return resourceDirectory;
    }
    
    public static File getMusicStorageDirectory() {
        return musicStorageDirectory;
    }
    
    @EventHandler
    public static void reloadPlayerResourcePack(PlayerResourcePackStatusEvent event){
        final Player p = event.getPlayer();
        if(reloadingPlayers.contains(p.getUniqueId()) && event.getStatus().equals(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)){
            new BukkitRunnable(){
                @Override
                public void run() {
                    sendPlayerResourcePack(p);
                }
            }.runTaskLater(Tiboise.getPlugin(),20);
        }
    }
    
    public static void rebuildResourcePack(){
        // unzip the downloaded source
        if(packSourceFile != null && packSourceFile.exists()){
            File tempDir = new File(packSourceFile.getParentFile().getPath()+File.separator+"temp");
            // decompress pack source
            try(ZipInputStream zis = new ZipInputStream(new FileInputStream(packSourceFile))){
                byte[] buffer = new byte[1024];
                
                ZipEntry zipEntry = zis.getNextEntry();
                if(tempDir.exists()){
                    FileUtils.deleteDirectory(tempDir);
                }
                tempDir.mkdirs();
                while (zipEntry != null) {
                    File newFile = new File(tempDir.getPath()+File.separator+zipEntry);
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
        
        File tempDir = new File(packSourceFile.getParentFile().getPath()+File.separator+"temp");
        
        // copy the data from local assets to the unzipped source
        if(tempDir.exists() && assets.exists()){
            try{
                // before copying merging the data from sounds.json
                // transferring downloaded audio from the music storage to the asset directory :
                File newSound = new File(getAssetDirectory().getPath()+File.separator+"minecraft"+File.separator+"sounds.json");
                String subDir = "music_discs";
                Set<String> loadedMusics = new HashSet<>();
                for(String indexedMusicId : MusicDisc.getIndexedMusics().keySet()){
                    File musicDir = new File(getMusicStorageDirectory().getPath()+File.separator+indexedMusicId);
                    if(musicDir.exists()){
                        // sound file names are all sound.ogg to allow better compatibility with tools like ffmpeg, some ids might start with a "-" which creates annoying errors to handle
                        File ogg = new File(musicDir.getPath()+File.separator+"sound.ogg");
                        File model = new File(musicDir.getPath()+File.separator+indexedMusicId+".json");
                        File cit = new File(musicDir.getPath()+File.separator+indexedMusicId+".properties");
                        File texture = new File(musicDir.getPath()+File.separator+indexedMusicId+".png");
                        if(ogg.exists() && model.exists() && cit.exists() && texture.exists()){
                            File targetOggs = new File(getAssetDirectory().getPath()+File.separator+"minecraft"+File.separator+"sounds"+File.separator+subDir+File.separator+indexedMusicId+".ogg");
                            File targetModels = new File(getAssetDirectory().getPath()+File.separator+"minecraft"+File.separator+"models"+File.separator+"item"+File.separator+subDir);
                            File targetCits = new File(getAssetDirectory().getPath()+File.separator+"minecraft"+File.separator+"optifine"+File.separator+"cit"+File.separator+subDir);
                            File targetTextures = new File(getAssetDirectory().getPath()+File.separator+"minecraft"+File.separator+"textures"+File.separator+"item"+File.separator+subDir);
                            
                            FileUtils.copyFile(ogg,targetOggs);
                            FileUtils.copyFileToDirectory(model,targetModels);
                            FileUtils.copyFileToDirectory(cit,targetCits);
                            FileUtils.copyFileToDirectory(texture,targetTextures);
                            // copying the index to allow users to find back the songs
                            FileUtils.copyFileToDirectory(getMusicIndexFile(),targetOggs);
                            
                            loadedMusics.add(indexedMusicId);
                        }
                    }
                }
                
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> merged = new HashMap<>();
                
                Map<String,Object> soundTemplate = new HashMap<>();
                try(InputStream in = Tiboise.getPlugin().getResource("discs/soundElement.json")){
                    soundTemplate = mapper.readValue(in,new TypeReference<HashMap<String, Object>>(){});
                } catch (JacksonException ignored){}
                
                File baseSound = new File(tempDir.getPath()+File.separator+"assets"+File.separator+"minecraft"+File.separator+"sounds.json");
                
                newSound.createNewFile();
                baseSound.createNewFile();
                
                
                
                
                if(baseSound.exists() && newSound.exists()){
                    try{
                        merged.putAll(mapper.readValue(baseSound,new TypeReference<HashMap<String, Object>>(){}));
                    } catch (JacksonException ignored){}
                    try{
                        merged.putAll(mapper.readValue(newSound,new TypeReference<HashMap<String, Object>>(){}));
                    } catch (JacksonException ignored){}
                    
                    for(String musicId : loadedMusics){
                        Map<String, Object> localMap = new HashMap<>(soundTemplate);
                        if(localMap.containsKey("sounds")) localMap.put("sounds",List.of("music_discs/"+musicId));
                        merged.put("tiboise.music_disc."+musicId,localMap);
                    }
                }
                
                FileUtils.copyDirectoryToDirectory(assets,tempDir);
                if(!merged.isEmpty()){
                    try(FileWriter writer = new FileWriter(baseSound)){
                        writer.write(mapper.writeValueAsString(merged));
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            // zip the merged data and transfer it to the webserver's directory for download
            File tempZipFile = new File(localResourcePackDirectory.getPath() + File.separator + "tempResourcePack.zip");
            try(
                    FileOutputStream out = new FileOutputStream(tempZipFile);
                    ZipOutputStream outZip = new ZipOutputStream(out);
            ){
                // zip the file
                File[] content = tempDir.listFiles();
                if(content!=null){
                    for(File file : content){
                        zipFile(file, file.getName(), outZip);
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            
            try{
                if(tempZipFile.exists()){
                    // copy the zipped result to the webserver's directory
                    File target = new File(webServerDirectory.getPath()+File.separator+packFileName);
                    target.delete();
                    Files.copy(tempZipFile.toPath(),target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    try(InputStream fis = new FileInputStream(target)){
                        MessageDigest digest = MessageDigest.getInstance("SHA-1");
                        
                        byte[] buffer = new byte[8192];
                        int len = fis.read(buffer);
                        
                        while (len != -1) {
                            digest.update(buffer, 0, len);
                            len = fis.read(buffer);
                        }
                        
                        String hash = Hex.encodeHexString(digest.digest());
                        packhash = hash;
                        Files.writeString(Path.of(webServerDirectory.getPath()+File.separator+hashFileName),hash);
                        logAdmin(packhash);
                        logAdmin("pack successfully built !");
                    }
                }
                
                tempZipFile.delete();
            } catch (
                    IOException |
                    NoSuchAlgorithmException e){
                e.printStackTrace();
            }
            
            
            // cleanup temporary files
            try{
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException e) {e.printStackTrace();}
        }
    }
    
    public static void sendPlayerResourcePack(Player p){
        if(getPackHash() != null){
            String hash = getPackHash().substring(0,Math.min(40,getPackHash().length()));
            logAdmin(hash);
            p.setResourcePack(getPackSource(), hash,true,Component.text("RPDL"));
            if(!reloadingPlayers.contains(p.getUniqueId())){
                reloadingPlayers.add(p.getUniqueId());
            } else {
                reloadingPlayers.remove(p.getUniqueId());
            }
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
    
    public static File getMusicIndexFile() {
        return musicIndexFile;
    }
    
    @EventHandler(priority = EventPriority.LOW)
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
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        downloadSourceResourcePack();
                        rebuildResourcePack();
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                File resourcePackBundled = new File(webServerDirectory.getPath()+File.separator+packFileName);
                                File resourcePackBundledHash = new File(webServerDirectory.getPath()+File.separator+hashFileName);
                                
                                if(resourcePackBundled.exists()){
                                    HttpServer server;
                                    try {
                                        server = HttpServer.create(new InetSocketAddress(webServerPort),8);
                                    } catch (
                                            IOException e) {
                                        throw new RuntimeException(e);
                                    }
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
                            }
                        }.runTask(Tiboise.getPlugin());
                    }
                }.runTaskAsynchronously(Tiboise.getPlugin());
                
                
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
            }
        }
    }
    
    
    public static void downloadSourceResourcePack(){
        try (BufferedInputStream in = new BufferedInputStream(new URL(packSource).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(packSourceFile)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
        }
    }
    
    
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            if(children != null){
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }
        
        try(FileInputStream fis = new FileInputStream(fileToZip)){
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
    
    public static File getAssetDirectory(){
        return assets;
    }
}
