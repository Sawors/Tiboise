package io.github.sawors.tiboise.exploration;

import io.github.sawors.tiboise.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class ExplorationGeneralFeatures implements Listener {
    
    private static Map<UUID, Set<PlayerCompassMarker>> markermap = new HashMap<>();
    
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
    
    
    public static void loadPlayerCompassMarkers(@NotNull Player p){
        World w = p.getWorld();
        File storage = new File(w.getWorldFolder()+File.separator+"markers"+File.separator+p.getUniqueId()+".yml");
        Set<PlayerCompassMarker> markers = new HashSet<>();
        if(storage.exists()){
        
        }
        
        markermap.put(p.getUniqueId(), markers);
    }
    
    public static void savePlayerCompassMarkers(@NotNull Player p){
        World w = p.getWorld();
        File storage = new File(w.getWorldFolder()+File.separator+"markers"+File.separator+p.getUniqueId()+".yml");
        Set<PlayerCompassMarker> markers = new HashSet<>();
        if(storage.exists()){
        
        }
        
        markermap.put(p.getUniqueId(), markers);
    }
    
    public static void addMarkerForPlayer(Player p, PlayerCompassMarker marker){
    
    }
    
    public static void removeMarkerForPlayer(Player p, UUID markerId){
    
    }
}
