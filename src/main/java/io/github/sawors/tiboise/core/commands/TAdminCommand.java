package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.PlayerResourcesManager;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class TAdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length > 0){
            final String tool = args[0];
            switch(tool){
                case "online", "list" -> {
                    StringBuilder msg = new StringBuilder("Online Players :");
                    for(Player p : Bukkit.getOnlinePlayers()){
                        msg.append(Component.text("\n - "+p.getName()+" : "+p.getUniqueId()));
                    }
                    logAdmin(msg);
                    return true;
                }
                case "print" -> {
                    if(sender instanceof Player p){
                        ItemStack i = p.getInventory().getItemInMainHand();
                        if(!i.getType().isAir()){
                            logAdmin(TiboiseItem.getPersistentDataPrint(i));
                        }
                    }
                }
                
                case "testmode" -> {
                    Tiboise.setTestMode(!Tiboise.isServerInTestMode());
                    String message = Tiboise.isServerInTestMode() ? "The server is now in test mode, BE CAREFUL ! This mode is not intended to be used on a live server." : "The server is no more in test mode.";
                    sender.sendMessage(Component.text(message).color(NamedTextColor.GREEN));
                }
                
                case "pack" -> {
                    if(args.length >= 2){
                        String action = args[1];
                        switch(action){
                            case "build" -> {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        PlayerResourcesManager.rebuildResourcePack();
                                    }
                                }.runTaskAsynchronously(Tiboise.getPlugin());
                            }
                            case "sync" -> {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        PlayerResourcesManager.downloadSourceResourcePack();
                                        PlayerResourcesManager.rebuildResourcePack();
                                    }
                                }.runTaskAsynchronously(Tiboise.getPlugin());
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}
