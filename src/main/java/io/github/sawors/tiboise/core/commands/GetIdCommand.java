package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.IdentifiedItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GetIdCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            ItemStack item = p.getInventory().getItemInMainHand();
            String itemidentifier = IdentifiedItem.getItemIdentifier(item);
            if(item.getType().equals(Material.STICK)){
                Entity e = p.getTargetEntity(8);
                if(e instanceof ArmorStand am){
                    am.setItem(EquipmentSlot.HEAD,item);
                }
                return true;
            }
            if(itemidentifier != null && item.hasItemMeta()){
                p.sendMessage("The identifier of item ["+ Tiboise.getComponentContent(item.displayName())+"] is "+itemidentifier);
            } else {
                p.sendMessage("This item has no identifier");
            }
            return true;
        }
        return false;
    }
}
