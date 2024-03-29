package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class THelpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            ItemStack item = p.getInventory().getItemInMainHand();
            p.sendMessage(
                    Component.text("Help for ").append(item.displayName()).append(Component.text(" : ")).color(NamedTextColor.GREEN)
                            .append(Component.text("\n "+TiboiseItem.getItemHelpText(item)).color(NamedTextColor.GRAY))
            );
            return true;
        }
        return false;
    }
}
