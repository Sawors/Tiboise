package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.core.SittingManager;
import org.bukkit.Axis;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            final float yaw = p.getLocation().getYaw();
            SittingManager.sitPlayerOnBlock(p,p.getLocation().add(0,-1,0).getBlock(), yaw >= 315 || yaw < 45 || (yaw >= 135 && yaw < 225) ? Axis.X : Axis.Z);
            return true;
        }
        return false;
    }
}
