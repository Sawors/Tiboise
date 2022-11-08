package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.IdentifiedItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GetIdCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            ItemStack item = p.getInventory().getItemInMainHand();
            UUID itemidentifier = IdentifiedItem.getItemIdentifier(item);
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
