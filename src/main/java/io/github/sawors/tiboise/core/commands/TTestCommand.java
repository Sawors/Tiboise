package io.github.sawors.tiboise.core.commands;

import org.bukkit.block.Block;
import org.bukkit.block.data.type.SculkSensor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player p){
            Block b = p.getTargetBlock(8);
            if(b!= null && b.getBlockData() instanceof SculkSensor sc1){
                sc1.setPhase(SculkSensor.Phase.ACTIVE);
                b.setBlockData(sc1);
            }
            return true;
        }
        return false;
    }
}
