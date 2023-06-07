package io.github.sawors.tiboise.economy.trade;

import io.github.sawors.tiboise.Tiboise;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TradingStation implements Listener {
    
    @EventHandler
    public static void displayCategory(PlayerInteractEvent event){
        final Block clicked = event.getClickedBlock();
        if(clicked != null && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND) && event.getAction().isRightClick()
            && clicked.getType().toString().contains("BUTTON")
        ){
            World w = clicked.getWorld();
            TextDisplay display = (TextDisplay) w.spawnEntity(clicked.getLocation().add(.5,.75,.5), EntityType.TEXT_DISPLAY);
            display.text(Component.text("Poissons"));
            display.setRotation(180,0);
            
            ItemDisplay itemDisplay = (ItemDisplay) w.spawnEntity(clicked.getLocation().add(.5,.33,.5),EntityType.ITEM_DISPLAY);
            itemDisplay.setItemStack(new ItemStack(Material.COD));
            itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            
            new BukkitRunnable(){
                @Override
                public void run(){
                    display.remove();
                    itemDisplay.remove();
                }
            }.runTaskLater(Tiboise.getPlugin(),20*6);
        }
    }
}
