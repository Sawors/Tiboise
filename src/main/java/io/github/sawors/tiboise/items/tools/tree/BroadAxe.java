package io.github.sawors.tiboise.items.tools.tree;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BroadAxe extends TreeCutter implements Listener {
    
    @EventHandler
    public static void onPlayerBreakTree(BlockBreakEvent event){
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        switch(tool.getType()){
            case IRON_AXE -> cutTreeFromBlock(event.getBlock(),32,CuttingMode.VERTICAL,tool);
            case DIAMOND_AXE -> cutTreeFromBlock(event.getBlock(),32,CuttingMode.VERTICAL_DOUBLE,tool);
            case NETHERITE_AXE -> cutTreeFromBlock(event.getBlock(),32,CuttingMode.EXTENDED,tool);
        }
    
    }
}
