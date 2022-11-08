package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.core.ItemTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemGlobalListeners implements Listener {

//    @EventHandler
//    public static void preventUsageInRecipes(InventoryClickEvent event){
//        Inventory top = event.getView().getTopInventory();
//        if(event.getCurrentItem() != null && TiboiseItem.getItemTags(event.getCurrentItem()).contains(ItemTag.PREVENT_USE_IN_CRAFTING.toString().toLowerCase()) && Tiboise.isCraftingInventory(top)){
//            event.setCancelled(true);
//        }
//    }

    @EventHandler
    public static void preventUsageInRecipeInput(CraftItemEvent event){
        ItemStack[] items = event.getInventory().getMatrix();
        for(ItemStack item : items){
            if(TiboiseItem.getItemTags(item).contains(ItemTag.PREVENT_USE_IN_CRAFTING.toString().toLowerCase())){
                event.setCancelled(true);
                break;
            }
        }
    }
}
