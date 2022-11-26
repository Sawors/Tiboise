package io.github.sawors.tiboise.exploration;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.sawors.tiboise.exploration.items.CopperCompass;
import io.github.sawors.tiboise.gui.GUIDisplayItem;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerCompassMarker implements Listener {
    // management
    private static Map<UUID, Set<PlayerCompassMarker>> loadedMarkersMap = new HashMap<>();
    private static Map<ItemStack, PlayerCompassMarker> showInventoryLink = new HashMap<>();
    // defaults
    public static final Material DEFAULT_MARKER_MATERIAL = Material.GLOBE_BANNER_PATTERN;
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    // unique marker
    private String name;
    private final String world;
    private MarkerVisualIcon icontype;
    private final UUID id;
    private final int x;
    private final int y;
    private final int z;
    
    private final LocalDateTime creationDate;
    
    private enum MarkerDataFields {
        NAME, WORLD, ICON_ITEM, ICON_TYPE, ID, X, Y, Z, CREATION_DATE
    }

    public enum MarkerVisualIcon {
        DEFAULT, RESOURCE, HOME, CROSS, STRUCTURE, RESOURCE_ALT, DEATH;
        public int getCustomModelValue(){
            return switch(this){
                // Arbitrary values, just to make it simple for players to change the model in a meaningful way
                // (no need to use hash here, there is not enough entries)
                case DEFAULT -> 1;
                case RESOURCE -> 2;
                case HOME -> 3;
                case CROSS -> 4;
                case STRUCTURE -> 5;
                case RESOURCE_ALT -> 6;
                case DEATH -> 7;
            };
        }
        
        public String getDisplayName(){
            return switch(this){
                // Arbitrary values, just to make it simple for players to change the model in a meaningful way
                // (no need to use hash here, there is not enough entries)
                case DEFAULT -> "Cross Marker";
                case RESOURCE -> "Resource Marker";
                case HOME -> "Home Marker";
                case CROSS -> "Cross 2 Marker";
                case STRUCTURE -> "Structure Marker";
                case RESOURCE_ALT -> "Resource 2 Marker";
                case DEATH -> "Skull Marker";
            };
        }
    }
    
    public PlayerCompassMarker(@NotNull String name, @NotNull Location location){
        this.name = name;
        this.world = location.getWorld().getName();
        this.icontype = MarkerVisualIcon.DEFAULT;
        this.id = UUID.randomUUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.creationDate = LocalDateTime.now();
    }
    
    public PlayerCompassMarker(@NotNull String name, @NotNull Location location, MarkerVisualIcon icon){
        this.name = name;
        this.world = location.getWorld().getName();
        this.icontype = icon;
        this.id = UUID.randomUUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.creationDate = LocalDateTime.now();
    }
    
    protected PlayerCompassMarker(@NotNull String name, @NotNull String world, MarkerVisualIcon icon, @NotNull UUID id, int x, int y, int z, LocalDateTime creationDate){
        this.name = name;
        this.world = world;
        this.icontype = icon;
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.creationDate = creationDate;
    }
    
    /**
     * SHOULD ONLY BE USED TO REGISTER THE LISTENER IN THE MAIN METHOD !!
     */
    public PlayerCompassMarker(){
        this.name = "Marker";
        this.world = "world";
        this.icontype = MarkerVisualIcon.DEFAULT;
        this.id = UUID.randomUUID();
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.creationDate = LocalDateTime.now();
    }
    
    public String getName() {
        return name;
    }
    
    public String getWorld() {
        return world;
    }
    
    public MarkerVisualIcon getIcon() {
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
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setIcon(MarkerVisualIcon icon){
        this.icontype = icon;
    }
    
    public @NotNull Location getDestination(){
        World w = Bukkit.getWorld(getWorld());
        w = w != null ? w : Bukkit.getWorlds().get(0);
        return new Location(w,getX(),getY(),getZ());
    }
    
    public ItemStack getDisplayItem(boolean tracking, boolean registerItem){
        return getDisplayItem(tracking,registerItem,false);
    }
    public ItemStack getDisplayItem(boolean tracking, boolean registerItem, boolean glint){
        ItemStack showitem = new ItemStack(DEFAULT_MARKER_MATERIAL);
        ItemMeta meta = showitem.getItemMeta();
        
        List<Component> lore = new ArrayList<>();
        if(tracking){
            lore.add(Component.text(ChatColor.GOLD+""+ChatColor.ITALIC+">> Left Click to track <<"));
            lore.add(Component.text(ChatColor.GREEN+""+ChatColor.ITALIC+">> Right Click to edit <<"));
        }
        lore.add(Component.text(ChatColor.GRAY+" x: "+getX()+" "));
        lore.add(Component.text(ChatColor.GRAY+" y: "+getY()+" "));
        lore.add(Component.text(ChatColor.GRAY+" z: "+getZ()+" "));
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.DARK_GRAY+"id: "+getId().toString().substring(0,8)+" "));
        
        meta.lore(lore);
        
        meta.displayName(Component.text(ChatColor.RED+getName()));

        // Should not be used in the future, only for indicative purposes
        meta.getPersistentDataContainer().set(GUIDisplayItem.getIconKey(), PersistentDataType.STRING, getIcon().toString().toLowerCase(Locale.ROOT));

        meta.setCustomModelData(getIcon().getCustomModelValue());
        
        showitem.setItemMeta(meta);
        
        if(glint){
            showitem.editMeta(m -> m.addItemFlags(ItemFlag.HIDE_ENCHANTS));
            showitem.addUnsafeEnchantment(Enchantment.DURABILITY,1);
        }
        
        if(registerItem){
            showInventoryLink.put(showitem,this);
        }
        
        return showitem;
    }
    
    public static boolean isLinked(ItemStack item){
        return showInventoryLink.containsKey(item);
    }
    
    public static @Nullable PlayerCompassMarker fromDisplay(ItemStack item){
        return showInventoryLink.get(item);
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
                    MarkerVisualIcon icontype = MarkerVisualIcon.DEFAULT;
                    try{
                        icontype = MarkerVisualIcon.valueOf(markerdata.getString(MarkerDataFields.ICON_TYPE.toString().toLowerCase()));
                    } catch (IllegalArgumentException ignored){}

                    UUID id = UUID.randomUUID();
                    try{
                        id = UUID.fromString(key);
                    } catch (IllegalArgumentException ignored){}
                    int x = markerdata.getInt(MarkerDataFields.X.toString().toLowerCase());
                    int y = markerdata.getInt(MarkerDataFields.Y.toString().toLowerCase());
                    int z = markerdata.getInt(MarkerDataFields.Z.toString().toLowerCase());
                    LocalDateTime creation = markerdata.getString(MarkerDataFields.CREATION_DATE.toString().toLowerCase()) != null ? LocalDateTime.parse(Objects.requireNonNull(markerdata.getString(MarkerDataFields.CREATION_DATE.toString().toLowerCase())),DEFAULT_DATETIME_FORMAT) : LocalDateTime.now();
                    name = name != null ? name : "Marker";
                    world = world != null ? world : Bukkit.getWorlds().get(0).getName();
                    
                    PlayerCompassMarker marker = new PlayerCompassMarker(name,world,icontype,id,x,y,z,creation);
                    markers.add(marker);
                }
            }
        }
        
        loadedMarkersMap.put(p.getUniqueId(), markers);
        
    }
    
    @CanIgnoreReturnValue
    public static void savePlayerCompassMarkers(@NotNull Player p){
        World w = p.getWorld();
        File storage = new File(w.getWorldFolder()+File.separator+"markers"+File.separator+p.getUniqueId()+".yml");
        try{
            if(!storage.exists()){
                new File(storage.getParent()).mkdirs();
                storage.createNewFile();
            }
            if(loadedMarkersMap.containsKey(p.getUniqueId())){
                YamlConfiguration markerdata = new YamlConfiguration();
                for(PlayerCompassMarker marker : loadedMarkersMap.get(p.getUniqueId())){
                    ConfigurationSection section =  markerdata.createSection(marker.getId().toString());
                    section.set(MarkerDataFields.NAME.toString().toLowerCase(), marker.getName());
                    section.set(MarkerDataFields.WORLD.toString().toLowerCase(), marker.getWorld());
                    section.set(MarkerDataFields.ICON_ITEM.toString().toLowerCase(), DEFAULT_MARKER_MATERIAL.toString());
                    section.set(MarkerDataFields.ICON_TYPE.toString().toLowerCase(), marker.getIcon().toString());
                    section.set(MarkerDataFields.X.toString().toLowerCase(), marker.getX());
                    section.set(MarkerDataFields.Y.toString().toLowerCase(), marker.getY());
                    section.set(MarkerDataFields.Z.toString().toLowerCase(), marker.getZ());
                    section.set(MarkerDataFields.CREATION_DATE.toString().toLowerCase(Locale.ROOT),marker.getCreationDate().format(DEFAULT_DATETIME_FORMAT));
                }
                markerdata.save(storage);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    
    
    public static void addMarkerForPlayer(Player p, PlayerCompassMarker marker){
        if(!loadedMarkersMap.containsKey(p.getUniqueId())){
            loadPlayerCompassMarkers(p);
        }
        Set<PlayerCompassMarker> markers = loadedMarkersMap.get(p.getUniqueId());
        if(markers.size() >= (CopperCompass.MARKER_INVENTORY_ROW_AMOUNT-2)*(9-2)) {
            p.sendMessage(Component.text(ChatColor.RED+"All your marker slots are already full !"));
            return;
        }
        // adding a marker with an already existing ID will overwrite its predecessor
        markers.removeIf(m -> m.getId().equals(marker.getId()));
        markers.add(marker);
        
        // added this to allow for compass tracking, should be removed when I find a way to track without Lodestone being placed at the destination
        //Block edit = marker.getDestination().clone().add(0,(-marker.getDestination().getY())-64,0).getBlock();
        //edit.setType(Material.LODESTONE);
        // THIS COULD LEAD TO EXPLOITS ! BE CAREFUL
        //edit.getRelative(BlockFace.UP).setType(Material.BEDROCK);
    }
    
    public static void removeMarkerForPlayer(Player p, UUID markerId){
        if(!loadedMarkersMap.containsKey(p.getUniqueId())){
            loadPlayerCompassMarkers(p);
        }
        Set<PlayerCompassMarker> pmarkers = loadedMarkersMap.get(p.getUniqueId());
        pmarkers.removeIf(m -> m.getId().equals(markerId));
    }
    
    public static @NotNull Set<PlayerCompassMarker> getPlayerMarkers(Player player){
        return loadedMarkersMap.get(player.getUniqueId()) != null ? loadedMarkersMap.get(player.getUniqueId()) : new HashSet<>();
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
        // unload offline players
        loadedMarkersMap.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public static void saveOnServerShutdown(PluginDisableEvent event){
        for(Player p : Bukkit.getOnlinePlayers()){
            savePlayerCompassMarkers(p);
        }
    }
    @EventHandler
    public static void loadPlayerMarkerOnJoin(PlayerJoinEvent event){
        loadPlayerCompassMarkers(event.getPlayer());
    }
    
    
}
