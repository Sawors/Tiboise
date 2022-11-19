package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class ResourcePackManager implements Listener {
    
    private final static String src = "https://github.com/Sawors/Tiboise/raw/master/src/main/resources/resourcepack/Tiboise-1.19.2.zip";
    private final static String hashfile = "https://github.com/Sawors/Tiboise/blob/master/src/main/resources/resourcepack/sha1.txt";
    
    @EventHandler
    public static void sendResourcePack(PlayerJoinEvent event){
        if(!Main.isServerInTestMode()){
            try(InputStream in = new URL(hashfile).openStream(); Scanner hashread = new Scanner(in)){
                event.getPlayer().setResourcePack("https://github.com/Sawors/Tiboise/raw/master/src/main/resources/resourcepack/Tiboise-1.19.2.zip",hashread.next(),true);
            }catch (IOException e){
                Main.logAdmin("Can't pass ResourcePack to players, malformed hash file URL");
            }
        }
        
    }
}
