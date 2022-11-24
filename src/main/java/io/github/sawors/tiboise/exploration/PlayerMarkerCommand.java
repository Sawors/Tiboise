package io.github.sawors.tiboise.exploration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PlayerMarkerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player p){
            if(args.length >= 1){
                switch(args[0]){
                    case "add" -> {
                        PlayerCompassMarker.addMarkerForPlayer(p,new PlayerCompassMarker("Marker",p.getLocation()));
                        PlayerCompassMarker.savePlayerCompassMarkers(p);}
                    case "list" -> {
                        Set<PlayerCompassMarker> markers = PlayerCompassMarker.getPlayerMarkers(p);
                        Component msg;
                        if(markers.size() == 0){
                            msg = Component.text(ChatColor.YELLOW+"You have no markers set");
                        }else {
                            msg = Component.text(ChatColor.GOLD+"Your markers are :");
                            for(PlayerCompassMarker mk : markers){
                                if(Objects.equals(mk.getWorld(), p.getWorld().getName())){
                                    msg = msg.append(Component.text("\n-"+ChatColor.GREEN+mk.getName())).append(Component.text(
                                             "\n    x: "+mk.getX()+
                                                    "\n    y: "+mk.getY()+
                                                    "\n    z: "+mk.getZ()
                                    ).color(TextColor.color(Color.YELLOW.asRGB())));
                                }
                            }
                        }
                        p.sendMessage(msg);
                    }
                }
            } else {
                final int rows = 6;
                Inventory showInv = Bukkit.createInventory(p,9*rows);
                List<PlayerCompassMarker> markers = List.copyOf(PlayerCompassMarker.getPlayerMarkers(p));
                int slot = 9;
                if(markers != null){
                    for(int i = 0; i < markers.size(); i++){
                        if(i>=9*(rows-1)){
                            break;
                        }
                        PlayerCompassMarker mk = markers.get(i);
                        int rowPosition = (slot % 9)+1;
                        // noobish way to do this but anyway
                        if(rowPosition == 1){
                            slot++;
                        } else if(rowPosition == 9){
                            slot+=2;
                        }
                        showInv.setItem(slot,mk.getDisplayItem(false,false));
                        slot++;
                    }
                }
                p.openInventory(showInv);
            }
            return true;
        }
        return false;
    }
}
