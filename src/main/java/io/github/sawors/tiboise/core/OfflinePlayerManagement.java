package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class OfflinePlayerManagement implements Listener {
    
    @EventHandler
    public static void sendPlayerVocalGreeting(PlayerResourcePackStatusEvent event){
        
        if(event.getStatus().equals(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)){
            Player p = event.getPlayer();
            String sound = "sawors.hello.default";
            if(p.getUniqueId().toString().equals("30b80f6f-f0dc-4b4a-96b2-c37b28494b1b") || p.getUniqueId().toString().equals("f96b1fab-2391-4c41-b6aa-56e6e91950fd")){
                sound = "sawors.hello."+p.getUniqueId();
            }
            final String fsound = sound;
            new BukkitRunnable(){
                @Override
                public void run() {
                    p.playSound(p.getLocation(),fsound,1,1);
                }
            }.runTaskLater(Main.getPlugin(),30);
    
            
        } else if(event.getStatus().equals(PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)){
            event.getPlayer().sendMessage(Component.text(ChatColor.GOLD+"There is an error in the loading of the resource pack.").hoverEvent(Component.text(ChatColor.YELLOW+"Click to reload").asHoverEvent()).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/tiboise pack")).color(TextColor.color(Color.RED.asRGB())));
        }
    }
    
}
