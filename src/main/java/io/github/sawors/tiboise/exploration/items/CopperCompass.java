package io.github.sawors.tiboise.exploration.items;

import io.github.sawors.tiboise.exploration.PlayerCompassMarker;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.List;

public class CopperCompass extends TiboiseItem implements Listener {
    
    private final static String MARKER_INVENTORY_NAME = "Markers";
    private final static int MARKER_INVENTORY_ROW_AMOUNT = 6;
    
    public CopperCompass(){
        setMaterial(Material.COMPASS);
    }
    
    @EventHandler
    public static void openMarkerInventory(PlayerInteractEvent event){
        if(event.getItem() != null && TiboiseItem.getItemId(event.getItem()).equals(new CopperCompass().getId()) && event.getAction().isRightClick()){
            Player p = event.getPlayer();
            Inventory showInv = Bukkit.createInventory(p,9*MARKER_INVENTORY_ROW_AMOUNT, Component.text(ChatColor.DARK_GRAY+MARKER_INVENTORY_NAME));
            List<PlayerCompassMarker> markers = List.copyOf(PlayerCompassMarker.getPlayerMarkers(p));
            int slot = 9;
            
            if(markers != null){
                for(int i = 0; i < markers.size(); i++){
                    if(i>=9*(MARKER_INVENTORY_ROW_AMOUNT-1)){
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
                    ItemStack display = mk.getDisplayItem(true,true);
                    showInv.setItem(slot,display);
                    slot++;
                }
            }
            p.openInventory(showInv);
        }
    }
    
    @EventHandler
    public static void onMarkerDisplayClick(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        PlayerCompassMarker marker = PlayerCompassMarker.fromDisplay(event.getCurrentItem());
        if(marker != null && inv.getSize() == 9*MARKER_INVENTORY_ROW_AMOUNT && inv.getHolder() instanceof Player p && PlayerCompassMarker.isLinked(event.getCurrentItem()) && TiboiseItem.getItemId(p.getInventory().getItemInMainHand()).equals(new CopperCompass().getId())){
            if(event.isLeftClick()){
                ItemStack compass = p.getInventory().getItemInMainHand();
                if(compass.getItemMeta() instanceof CompassMeta cm){
                    cm.setLodestoneTracked(true);
                    cm.setLodestone(marker.getDestination().add(0,(-marker.getDestination().getY())-64,0));
                    compass.setItemMeta(cm);
                    event.setCancelled(true);
                    p.closeInventory();
                }
            } else {
            
            }
        }
    }
}
