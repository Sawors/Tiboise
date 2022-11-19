package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class ResourcePackManager implements Listener {
    
    private final static String src = "https://github.com/Sawors/Tiboise/raw/master/src/main/resources/resourcepack/Tiboise-1.19.2.zip";
    private final static String hashfile = "https://raw.githubusercontent.com/Sawors/Tiboise/master/src/main/resources/resourcepack/sha1.txt";
    private static String packhash = null;
    
    @EventHandler
    public static void sendResourcePackOnJoin(PlayerJoinEvent event){
        if(!Main.isServerInTestMode()){
            new BukkitRunnable(){
                @Override
                public void run() {
                    event.getPlayer().setResourcePack(src,packhash,true);
                }
            }.runTaskLater(Main.getPlugin(),20);
        }
    }
    
    public static void reloadPackData(){
        try(InputStream in = new URL(hashfile).openStream(); Scanner hashread = new Scanner(in)){
            packhash = hashread.next();
        }catch (IOException e){
            Main.logAdmin("Can't load resource pack, malformed hash file URL");
        }
    }
    
    public static String getPackHash(){
        return packhash;
    }
    
    public static String getPackSource(){
        return src;
    }
}
