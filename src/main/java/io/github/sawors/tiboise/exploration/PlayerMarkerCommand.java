package io.github.sawors.tiboise.exploration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerMarkerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player p){
            PlayerCompassMarker.addMarkerForPlayer(p,new PlayerCompassMarker("Marker",p.getLocation()));
        }
        return false;
    }
}
