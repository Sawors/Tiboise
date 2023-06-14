package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.core.local.ResourcePackManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            ResourcePackManager.sendPlayerResourcePack(p);
            return true;
        }
        return false;
    }
}
