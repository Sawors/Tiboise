package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.items.discs.MusicDisc;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class GiveItemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1 && sender instanceof Player p){
            TiboiseItem item = TiboiseItem.getRegisteredItem(args[0]);
            if(item == null) {
                sender.sendMessage(Component.text("The item ["+args[0]+"] does not exist"));
                return true;
            }
            
            
            
            if(args.length >= 2){
                if(item instanceof MusicDisc){
                    final String name = Arrays.stream(args).reduce((c1,c2) -> c1+" "+c2).get().replaceFirst(args[0]+" ","");
                    final String lookup = MusicDisc.lookupMusicName(name);
                    Pair<String,String> musicData = MusicDisc.parseTitleString(lookup != null ? lookup : name);
                    item = new MusicDisc(musicData.getKey(),musicData.getValue());
                } else {
                    String variant = args[1];
                    
                    item.setVariant(variant);
                }
            }
            p.getInventory().addItem(item.get());
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
