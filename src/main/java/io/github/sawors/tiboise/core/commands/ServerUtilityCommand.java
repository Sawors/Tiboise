package io.github.sawors.tiboise.core.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ServerUtilityCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length > 0){
            final String tool = args[0];
            switch(tool){
                case "online", "list" -> {
                    Component msg = Component.text("Online Players :");
                    for(Player p : Bukkit.getOnlinePlayers()){
                        msg = msg.append(Component.text("\n - "+p.getName()));
                    }
                    sender.sendMessage(msg);
                    return true;
                }
            }
        }
        return false;
    }
}
