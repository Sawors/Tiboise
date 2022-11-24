package io.github.sawors.tiboise.exploration;

import io.github.sawors.tiboise.Main;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerCompassMarker {
    String name;
    String world;
    ItemStack iconitem;
    String icontype;
    UUID id;
    int x;
    int y;
    int z;
    
    public PlayerCompassMarker(String name, Location location){
    
    }
    
    public PlayerCompassMarker(String name, Location location, ItemStack icon){
    
    }
    
    public PlayerCompassMarker(String name, Location location, ItemStack icon, String icontype){
    
    }
    
    public String getName() {
        return name;
    }
    
    public String getWorld() {
        return world;
    }
    
    public ItemStack getIconitem() {
        return iconitem;
    }
    
    public String getIcontype() {
        return icontype;
    }
    
    public UUID getId() {
        return id;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    protected static NamespacedKey getIconTypeKey(){
        return new NamespacedKey(Main.getPlugin(), "icon");
    }
    
}
