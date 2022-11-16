package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length >= 1){
            try{
                Tiboise.logAdmin("Trying to split value "+args[0]+" in coins");
                List<ItemStack> coins = CoinItem.getCoinsForValue(Integer.parseInt(args[0]));
                int total = 0;
                for(ItemStack i : coins){
                    String variant = TiboiseItem.getItemVariant(i);
                    Tiboise.logAdmin("-> Coin "+ variant +" with value "+CoinItem.getCoinValue(variant));
                    total += CoinItem.getCoinValue(variant);
                }
                Tiboise.logAdmin("total: "+total);
                return true;
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
