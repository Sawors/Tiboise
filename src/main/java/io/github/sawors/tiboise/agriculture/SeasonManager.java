package io.github.sawors.tiboise.agriculture;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeasonManager implements Listener {
    // TODO : Move this to the config file
    private static final int yearLength = 365;
    private static Map<UUID,GameTime> currentTimes = new HashMap<>();
    
    
    @EventHandler
    public static void saveSeasonOnWorldSave(WorldSaveEvent event){
        Bukkit.getWorlds().get(0);
    }
    
    @EventHandler
    public static void initializeSeasonCycle(PluginEnableEvent event){
        if(event.getPlugin().equals(Tiboise.getPlugin())){
        
        }
    }
    
    public static void displayDayAnnouncement(World world){
    }
    
    
    
    public class GameTime {
    
    }
    
    public enum GameMonth {
        JANUARY,
        FEBRUARY,
        MARCH,
        APRIL,
        MAY,
        JUNE,
        JULY,
        AUGUST,
        SEPTEMBER,
        OCTOBER,
        NOVEMBER,
        DECEMBER
    }
}




