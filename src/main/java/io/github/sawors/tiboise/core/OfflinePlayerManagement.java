package io.github.sawors.tiboise.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class OfflinePlayerManagement implements Listener {
    
    @EventHandler
    public static void sendPlayerVocalGreeting(PlayerResourcePackStatusEvent event){
        if(event.getStatus().equals(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)){
            Player p = event.getPlayer();
            String sound = "sawors.hello.default";
            if(p.getUniqueId().toString().equals("30b80f6f-f0dc-4b4a-96b2-c37b28494b1b") || p.getUniqueId().toString().equals("f96b1fab-2391-4c41-b6aa-56e6e91950fd")){
                sound = "sawors.hello."+p.getUniqueId();
            }
            
            p.playSound(p.getLocation(),sound,1,1);
            
        }
    }
    
}
