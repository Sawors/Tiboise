package io.github.sawors.tiboise.core;

import io.papermc.paper.world.MoonPhase;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;
import java.util.Locale;

public class SpawnManager implements Listener {
    
    private static List<EntityType> exceptions = List.of(
          EntityType.DROWNED,
          EntityType.CAVE_SPIDER,
          EntityType.SPIDER,
          EntityType.VEX,
          EntityType.EVOKER,
          EntityType.EVOKER_FANGS,
          EntityType.ILLUSIONER,
          EntityType.PILLAGER,
          EntityType.RAVAGER,
          EntityType.WARDEN,
          EntityType.WITHER,
          EntityType.ENDER_DRAGON,
          EntityType.ZOMBIE_VILLAGER,
          EntityType.WITCH,
          EntityType.SILVERFISH
    );
    
    private static List<CreatureSpawnEvent.SpawnReason> blockedreasons = List.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
            CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
            CreatureSpawnEvent.SpawnReason.SPAWNER
    );
    
    @EventHandler
    public static void preventMobSpawn(CreatureSpawnEvent event){
        if(event.getEntity() instanceof Warden){
            return;
        }
        if(event.getEntity() instanceof Monster m && blockedreasons.contains(event.getSpawnReason())){
            EntityType type = m.getType();
            event.setCancelled(true);
                if(exceptions.contains(type) && Math.random() <= .1){
                    switch (type){
                        case SPIDER -> {
                            Location spawnloc = m.getLocation();
                            if(spawnloc.getY()<=32 || spawnloc.getWorld().getBiome(spawnloc).getKey().getKey().toLowerCase(Locale.ROOT).contains("forest") && Math.random() < .1){
                                event.setCancelled(false);
                            }
                        }
                        case WITCH -> {
                            if(m.getLocation().getWorld().getMoonPhase().equals(MoonPhase.FULL_MOON)){
                                event.setCancelled(false);
                            }
                        }
                        default -> {event.setCancelled(false);}
                    }
                }
        }
    }
}
