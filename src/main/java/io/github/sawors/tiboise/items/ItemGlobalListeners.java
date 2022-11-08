package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.core.ItemTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.*;

public class ItemGlobalListeners implements Listener {

    @EventHandler
    public static void preventUsageInRecipes(InventoryInteractEvent event){
        if( !(
                event.getInventory() instanceof PlayerInventory ||
                event.getInventory() instanceof CraftingInventory ||
                event.getInventory() instanceof AbstractHorseInventory ||
                event.getInventory() instanceof DoubleChestInventory ||
                event.getInventory() instanceof LecternInventory
                )){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void preventUsageInRecipeInput(CraftItemEvent event){
        ItemStack[] items = event.getInventory().getMatrix();
        for(ItemStack item : items){
            if(TiboiseItem.getItemTags(item).contains(ItemTag.PREVENT_USE_IN_CRAFTING.toString())){
                event.setCancelled(true);
                break;
            }
        }
    }
}
