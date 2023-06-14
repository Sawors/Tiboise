package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.core.local.ResourcePackManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TiboiseMainCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length >= 1){
            switch(args[0]){
                case "reload":
                    if(args.length >= 2 && sender.isOp()){
                        switch (args[1]){
                            case "packs":
                            case "pack":
                                ResourcePackManager.rebuildResourcePack();
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    ResourcePackManager.sendPlayerResourcePack(p);
                                }
                        }
                    }
            }
        }
        return false;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length >= 1){
            switch(strings[0]){
                case "action" -> {return List.of("reload");}
                case "target" -> {return List.of("packs");}
            }
        }
        return null;
    }
}
