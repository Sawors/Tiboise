package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveItemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1 && sender instanceof Player p){
            TiboiseItem item = null;
            if(args.length >= 2){
                String variant = args[1];
                item = Tiboise.getRegisteredItem(args[0]);
                item.setVariant(variant);
            } else {
                item = Tiboise.getRegisteredItem(args[0]);
            }
            if(item != null){
                p.getInventory().addItem(item.get());
            }
            return true;
        }
        return false;
    }
}

/*
* Player p = ((Player) sender).getPlayer();
            try{
                SItem itemname = SItem.valueOf(args[0].toUpperCase());
                ItemStack item = StonesItems.get(itemname);
                if(p != null){
                    p.getInventory().addItem(item);
                }
                return true;
            } catch (IllegalArgumentException exc){
                if(p != null){
                    p.sendMessage(ChatColor.RED + "this item does not exist in the available items");
                }
                return false;
            }
            *
            *
*/
