package io.github.sawors.tiboise.economy.trade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class TradingStationCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player && args.length >= 1){
            UUID pid = player.getUniqueId();
            RayTraceResult raytrace = player.rayTraceBlocks(4);
            if(raytrace != null){
                Block target = raytrace.getHitBlock();
                // the target MUST be the sign representing the shop, checks to find the sign assigned to a barrel will NOT be made
                logAdmin("check 1");
                if(target != null && target.getState() instanceof PersistentDataHolder holder && target.getState() instanceof Sign){
                    PersistentDataContainer container = holder.getPersistentDataContainer();
                    logAdmin("check 2");
                    TradingStation station = TradingStation.fromBlock(target);
                    if(station == null) return false;
                    String cmd = args[0];
                    if(!station.getOwner().equals(pid)){
                        if(sender.isOp()){
                            sender.sendMessage(Component.text("You are editing a shop owned by another player, be careful").color(NamedTextColor.GOLD));
                        } else {
                            sender.sendMessage(Component.text("You do not have the permission to edit this shop").color(NamedTextColor.RED));
                            return true;
                        }
                    }
                    switch (cmd){
                        case "transfer" -> {
                            if(sender.isOp() && args.length >= 2){
                                String to = args[1];
                                OfflinePlayer p2 = Bukkit.getOfflinePlayerIfCached(to);
                                if(p2 != null){
                                    UUID toId = p2.getUniqueId();
                                    station.setOwner(toId);
                                    station.save();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
