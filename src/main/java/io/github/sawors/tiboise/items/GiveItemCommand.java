package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.economy.CoinItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveItemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1 && sender instanceof Player p){
            switch(args[0]){
                case "coin" -> {
                    String variant = "copper";
                    if(args.length >= 2){
                        variant = args[1];
                        CoinItem coin = new CoinItem();
                        coin.setCoinVariant(variant);

                        p.getInventory().addItem(coin.get());
                    }
                }
            }
        }
        return false;
    }
}
