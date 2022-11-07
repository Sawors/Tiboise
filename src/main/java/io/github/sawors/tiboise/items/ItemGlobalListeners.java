package io.github.sawors.tiboise.items;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.*;

public class ItemGlobalListeners implements Listener {

    @EventHandler
    public static void preventUsageInRecipes(InventoryInteractEvent event){
        if(!(
                event.getInventory() instanceof PlayerInventory ||
                event.getInventory() instanceof CraftingInventory ||
                event.getInventory() instanceof AbstractHorseInventory ||
                event.getInventory() instanceof DoubleChestInventory ||
                event.getInventory() instanceof LecternInventory
                )){
            event.setCancelled(true);
        }
    }
}
