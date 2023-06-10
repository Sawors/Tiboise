package io.github.sawors.tiboise.core;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class VillagerManager implements Listener {
    
    @EventHandler
    public static void noVillagerTradeAndDialogue(PlayerInteractEntityEvent event){
        if(event.getRightClicked() instanceof Villager villager){
            event.setCancelled(true);
            
            // TODO : Villager dialogue
        }
    }
}
