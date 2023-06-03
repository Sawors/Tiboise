package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class FloatingTextUtils implements Listener {
    
    public static final String cleanupDisplayTag = "cleanup";
    
    public static void cleanupTempDisplays(World world){
        for(Display temp : world.getEntitiesByClass(Display.class)){
            if(temp.getPersistentDataContainer().get(getTemporaryTagKey(), PersistentDataType.STRING) != null){
                temp.remove();
            }
        }
    }
    
    public static NamespacedKey getTemporaryTagKey(){
        return new NamespacedKey(Tiboise.getPlugin(), "temporary_entity_tag");
    }
    
    public static void createTempTextDisplay(Location loc, Component text, int lifetime){
        TextDisplay display = (TextDisplay) loc.getWorld().spawnEntity(loc,EntityType.TEXT_DISPLAY);
        display.text(text);
        display.getPersistentDataContainer().set(getTemporaryTagKey(),PersistentDataType.STRING,cleanupDisplayTag);
        display.setSeeThrough(true);
        
        new BukkitRunnable(){
            @Override
            public void run() {
                if(display.isValid()){
                    display.remove();
                }
            }
        }.runTaskLater(Tiboise.getPlugin(),lifetime);
    }
    
    public static void registerForCleanup(Display display){
        display.getPersistentDataContainer().set(getTemporaryTagKey(),PersistentDataType.STRING,cleanupDisplayTag);
    }
}
