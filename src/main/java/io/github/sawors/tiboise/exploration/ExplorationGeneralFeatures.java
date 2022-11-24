package io.github.sawors.tiboise.exploration;

import io.github.sawors.tiboise.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ExplorationGeneralFeatures implements Listener {
    
    
    
    //
    //  COMPASS NORTH
    @EventHandler
    public void setCompassNorth(PlayerChangedWorldEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Player p = event.getPlayer();
                p.setCompassTarget(new Location(p.getWorld(), 0,0,-1000000));
                p.updateInventory();
            }
        }.runTask(Main.getPlugin());
    }
    @EventHandler
    public void setCompassNorth(PlayerJoinEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Player p = event.getPlayer();
                p.setCompassTarget(new Location(p.getWorld(), 0,0,-1000000));
                p.updateInventory();
            }
        }.runTask(Main.getPlugin());
    }
    @EventHandler
    public void setCompassNorth(PlayerBedLeaveEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Player p = event.getPlayer();
                p.setCompassTarget(new Location(p.getWorld(), 0,0,-1000000));
                p.updateInventory();
            }
        }.runTask(Main.getPlugin());
    }
    
    
    
}
