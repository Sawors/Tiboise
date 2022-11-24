package io.github.sawors.tiboise.exploration;

import io.github.sawors.tiboise.Main;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerCompassMarker implements Listener {
    // management
    private static Map<UUID, Set<PlayerCompassMarker>> loadedmarkermap = new HashMap<>();
    // defaults
    private static final Material DEFAULT_MARKER_MATERIAL = Material.MAP;
    private static final String DEFAULT_MARKER_TYPE = "default";
    // unique marker
    private String name;
    private String world;
    private Material iconitem;
    private String icontype;
    private UUID id;
    private int x;
    private int y;
    private int z;
    
    private enum MarkerDataFields {
        NAME, WORLD, ICON_ITEM, ICON_TYPE, ID, X, Y, Z
    }
    
    public PlayerCompassMarker(@NotNull String name, @NotNull Location location){
        this.name = name;
        this.world = location.getWorld().getName();
        this.iconitem = DEFAULT_MARKER_MATERIAL;
        this.icontype = DEFAULT_MARKER_TYPE;
        this.id = UUID.randomUUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.x = location.getBlockZ();
        
    }
    
    public PlayerCompassMarker(@NotNull String name, @NotNull Location location, @NotNull Material icon){
        this.name = name;
        this.world = location.getWorld().getName();
        this.iconitem = icon;
        this.icontype = DEFAULT_MARKER_TYPE;
        this.id = UUID.randomUUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.x = location.getBlockZ();
    }
    
    public PlayerCompassMarker(@NotNull String name, @NotNull Location location, @NotNull Material icon, @NotNull String icontype){
        this.name = name;
        this.world = location.getWorld().getName();
        this.iconitem = icon;
        this.icontype = icontype;
        this.id = UUID.randomUUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.x = location.getBlockZ();
    }
    
    protected PlayerCompassMarker(@NotNull String name, @NotNull String world, @NotNull Material iconitem, @NotNull String icontype, @NotNull UUID id, int x, int y, int z){
        this.name = name;
        this.world = world;
        this.iconitem = iconitem;
        this.icontype = icontype;
        this.id = id;
        this.x = x;
        this.y = y;
        this.x = z;
    }
    
    public String getName() {
        return name;
    }
    
    public String getWorld() {
        return world;
    }
    
    public Material getIconitem() {
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
    
    public static void loadPlayerCompassMarkers(@NotNull Player p){
        World w = p.getWorld();
        File storage = new File(w.getWorldFolder()+File.separator+"markers"+File.separator+p.getUniqueId()+".yml");
        Set<PlayerCompassMarker> markers = new HashSet<>();
        if(storage.exists()){
            YamlConfiguration data = YamlConfiguration.loadConfiguration(storage);
            for(String key : data.getKeys(false)){
                ConfigurationSection markerdata = data.getConfigurationSection(key);
                if(markerdata != null){
                    String name = markerdata.getString(MarkerDataFields.NAME.toString().toLowerCase());
                    String world = markerdata.getString(MarkerDataFields.WORLD.toString().toLowerCase());
                    Material iconitem = DEFAULT_MARKER_MATERIAL;
                    try{
                        iconitem = Material.valueOf(markerdata.getString(MarkerDataFields.ICON_ITEM.toString().toLowerCase()));
                    } catch (IllegalArgumentException | NullPointerException ignored){}
                    String icontype = markerdata.getString(MarkerDataFields.ICON_TYPE.toString().toLowerCase());
                    UUID id = UUID.randomUUID();
                    try{
                        id = UUID.fromString(key);
                    } catch (IllegalArgumentException ignored){}
                    int x = markerdata.getInt(MarkerDataFields.X.toString().toLowerCase());
                    int y = markerdata.getInt(MarkerDataFields.Y.toString().toLowerCase());
                    int z = markerdata.getInt(MarkerDataFields.Z.toString().toLowerCase());
                    name = name != null ? name : "Marker";
                    world = world != null ? world : Bukkit.getWorlds().get(0).getName();
                    icontype = icontype != null ? icontype : DEFAULT_MARKER_TYPE;
                    
                    markers.add(new PlayerCompassMarker(name,world,iconitem,icontype,id,x,y,z));
                }
            }
        }
        
        loadedmarkermap.put(p.getUniqueId(), markers);
    }
    
    public static void savePlayerCompassMarkers(@NotNull Player p){
        World w = p.getWorld();
        File storage = new File(w.getWorldFolder()+File.separator+"markers"+File.separator+p.getUniqueId()+".yml");
        try{
            storage.createNewFile();
            
            if(loadedmarkermap.containsKey(p.getUniqueId())){
                YamlConfiguration markerdata = YamlConfiguration.loadConfiguration(storage);
                for(PlayerCompassMarker marker : loadedmarkermap.get(p.getUniqueId())){
                    ConfigurationSection section =  markerdata.createSection(marker.getId().toString());
                    section.set(MarkerDataFields.NAME.toString().toLowerCase(), marker.getName());
                    section.set(MarkerDataFields.WORLD.toString().toLowerCase(), marker.getWorld());
                    section.set(MarkerDataFields.ICON_ITEM.toString().toLowerCase(), marker.getIconitem());
                    section.set(MarkerDataFields.ICON_TYPE.toString().toLowerCase(), marker.getIcontype());
                    section.set(MarkerDataFields.X.toString().toLowerCase(), marker.getX());
                    section.set(MarkerDataFields.Y.toString().toLowerCase(), marker.getY());
                    section.set(MarkerDataFields.Z.toString().toLowerCase(), marker.getZ());
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    
    
    public static void addMarkerForPlayer(Player p, PlayerCompassMarker marker){
        if(!loadedmarkermap.containsKey(p.getUniqueId())){
            loadPlayerCompassMarkers(p);
        }
        loadedmarkermap.get(p.getUniqueId()).add(marker);
    }
    
    public static void removeMarkerForPlayer(Player p, UUID markerId){
        if(!loadedmarkermap.containsKey(p.getUniqueId())){
            loadPlayerCompassMarkers(p);
        }
        Set<PlayerCompassMarker> pmarkers = loadedmarkermap.get(p.getUniqueId());
        for(PlayerCompassMarker marker : pmarkers){
            if(marker.getId().equals(markerId)){
                pmarkers.remove(marker);
            }
        }
    }
    
    @EventHandler
    public static void savePlayerMarkerOnWorldSave(WorldSaveEvent event){
        for(Player p : Bukkit.getOnlinePlayers()){
            savePlayerCompassMarkers(p);
        }
    }
    @EventHandler
    public static void savePlayerMarkerOnLeave(PlayerQuitEvent event){
        savePlayerCompassMarkers(event.getPlayer());
    }
    @EventHandler
    public static void loadPlayerMarkerOnJoin(PlayerJoinEvent event){
        loadPlayerCompassMarkers(event.getPlayer());
    }
    
    
}
