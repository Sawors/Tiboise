package io.github.sawors.tiboise.integrations.bungee;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KidnapCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length >= 2){
            String target = strings[0];
            String destination = strings[1];
            Player p = Bukkit.getPlayer(target);
            if(p != null && commandSender.isOp()){
                BungeeListener.movePlayerToServer(p,destination);
                return true;
            }
        }
        return false;
    }
}
