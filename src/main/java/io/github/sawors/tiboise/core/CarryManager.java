package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CarryManager {
    private static Map<UUID, Material> materialMap = new HashMap();
    private static Map<UUID, ItemStack[]> dataMap = new HashMap<>();
    
    
    
    
    public static void makePlayerCarryBlock(Player player, Block block){
        if(block.getState() instanceof BlockInventoryHolder container){
            materialMap.put(player.getUniqueId(),block.getType());
            dataMap.put(player.getUniqueId(), container.getInventory().getContents());
            block.setType(Material.AIR);
        }
        Tiboise.logAdmin(materialMap);
        Tiboise.logAdmin(dataMap);
    }
    
    public static void makePlayerDropCarry(Player player){
        if(materialMap.containsKey(player.getUniqueId())){
            Block b = player.getTargetBlock(4);
            BlockState state = b.getState();
            Tiboise.logAdmin("setting");
            Tiboise.logAdmin(materialMap);
            Tiboise.logAdmin(dataMap);
            state.setType(materialMap.get(player.getUniqueId()));
            Tiboise.logAdmin(dataMap.containsKey(player.getUniqueId()));
            if(dataMap.containsKey(player.getUniqueId())){
                ItemStack[] storage = dataMap.get(player.getUniqueId());
                Tiboise.logAdmin(storage);
                if(state instanceof BlockInventoryHolder container && storage.length > 0 && state.update(true,true)){
                    container.getInventory().setContents(storage);
                }
            }
            state.update(true,true);
            materialMap.remove(player.getUniqueId());
            dataMap.remove(player.getUniqueId());
            Tiboise.logAdmin(materialMap);
            Tiboise.logAdmin(dataMap);
        }
        
    }
}
