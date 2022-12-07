package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
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

public class ResourcePackManager implements Listener {
    
    private final static String src = "https://github.com/Sawors/Tiboise/raw/master/src/main/resources/resourcepack/Tiboise-1.19.2.zip";
    private final static String hashfile = "https://raw.githubusercontent.com/Sawors/Tiboise/master/src/main/resources/resourcepack/sha1.txt";
    private static HashSet<UUID> reloadingPlayers = new HashSet<>();
    private static String packhash = null;
    
    @EventHandler
    public static void sendResourcePackOnJoin(PlayerJoinEvent event){
        if(!Tiboise.isServerInTestMode()){
            new BukkitRunnable(){
                @Override
                public void run() {
                    sendPlayerResourcePack(event.getPlayer());
                }
            }.runTaskLater(Tiboise.getPlugin(),60);
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
