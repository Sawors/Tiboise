package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.integrations.voicechat.VoiceChatIntegrationPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.UUID;

public class ClientDataManager implements Listener {
    
    private final static String src = "https://github.com/Sawors/Tiboise/raw/master/src/main/resources/resourcepack/Tiboise-1.19.2.zip";
    private final static String hashfile = "https://raw.githubusercontent.com/Sawors/Tiboise/master/src/main/resources/resourcepack/sha1.txt";
    private static HashSet<UUID> reloadingPlayers = new HashSet<>();
    private static String packhash = null;
    
    @EventHandler
    public static void checkResourcesOnJoin(PlayerJoinEvent event){
        final Player p = event.getPlayer();
        if(!Tiboise.isServerInTestMode()){
            new BukkitRunnable(){
                @Override
                public void run() {
                    sendPlayerResourcePack(event.getPlayer());
                    
                    if(VoiceChatIntegrationPlugin.getVoicechatServerApi().getConnectionOf(p.getUniqueId()) == null){
                        p.sendMessage(Component.text(ChatColor.RED+"You do not seem to have the simple voice chat mod installed. Please click ")
                                .append(Component.text(ChatColor.GOLD+""+ChatColor.UNDERLINE+"HERE").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,"https://minhaskamal.github.io/DownGit/#/home?url=https:%2F%2Fgithub.com%2FSawors%2Fmc-dev-server%2Fblob%2Fmain%2Fmods%20tiboise%20updated.zip")))
                                .append(Component.text(ChatColor.RED+" in order to install the server recommended modpack, or "))
                                .append(Component.text(ChatColor.GOLD+"here").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,"https://www.curseforge.com/minecraft/mc-mods/simple-voice-chat")))
                                .append(Component.text(ChatColor.RED+" if you want the mod alone."))
                        );
                    }
                    
                }
            }.runTaskLater(Tiboise.getPlugin(),20*4);
        }
        
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
        try(InputStream in = new URL(hashfile).openStream(); Scanner hashread = new Scanner(in)){
            packhash = hashread.next();
        }catch (IOException e){
            Tiboise.logAdmin("Can't load resource pack, malformed hash file URL");
        }
    }
    
    public static void sendPlayerResourcePack(Player p){
        p.setResourcePack(src,packhash);
    }
    
    public static String getPackHash(){
        return packhash;
    }
    
    public static String getPackSource(){
        return src;
    }
}
