package io.github.sawors.tiboise.post;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.github.sawors.tiboise.Tiboise;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class PostLetterBox implements Listener {
    
    private final static String signIdentifier = "- Poste -";
    private final static Map<UUID, Set<PostLetterBox>> loadedLetterboxes = new HashMap<>();
    private final static Set<Material> allowedContainer = Set.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL
    );
    // Format :
    //    - Poste -
    //      owner
    //   house_name
    
    
    @EventHandler
    public static void plel(AsyncChatEvent event){
        logAdmin(loadedLetterboxes);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void recogniseLetterBox(SignChangeEvent event){
        final Block b = event.getBlock();
        final Player p = event.getPlayer();
        if(!event.isCancelled() && b.getState() instanceof Sign sign && b.getBlockData() instanceof Directional wallSign){
            final String ownerId = sign.getPersistentDataContainer().get(getOwnerKey(),PersistentDataType.STRING);
            // prevents other people from editing the sign (but not destroying it however)
            if(ownerId != null && !ownerId.equals(p.getUniqueId().toString())) {
                event.setCancelled(true);
                return;
            }
            if(event.lines().size() >= 1){
                final Component identifier = event.line(0);
                if(((TextComponent) Objects.requireNonNull(identifier)).content().equals(signIdentifier)){
                    // sign is recognised as a post sign
                    final Block relative = b.getRelative(wallSign.getFacing().getOppositeFace());
                    if(allowedContainer.contains(relative.getType()) && sign.lines().size() >= 3 && event.line(1) != null){
                        
                        PostLetterBox letterBox = new PostLetterBox(p.getUniqueId(),sign.getBlock());
                        letterBox.save();
                        Set<PostLetterBox> locs = loadedLetterboxes.getOrDefault(p.getUniqueId(),new HashSet<>());
                        locs.add(letterBox);
                        loadedLetterboxes.put(p.getUniqueId(),locs);
                        sign.getPersistentDataContainer().set(getOwnerKey(), PersistentDataType.STRING,p.getUniqueId().toString());
                        sign.update();
                        
                        event.line(1,Component.text(p.getName()));
                        p.sendActionBar(Component.text("House ").append(Component.text(letterBox.getName()).decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE)).append(Component.text(" successfully registered !")).color(NamedTextColor.GOLD));
                        final BlockFace face = wallSign.getFacing();
                        final Vector facing = face.getDirection();
                        b.getWorld().spawnParticle(Particle.WAX_ON,b.getLocation().add(.5,.5,.5).add(face.getDirection().multiply(-.30)),6,facing.getZ()/4,.20,facing.getX()/4,.1);
                        
                        
                        /*try {
                            final File saveFile = new File(b.getWorld().getWorldFolder().getCanonicalFile()+File.separator+"mailboxes"+File.separator+p.getUniqueId()+".yml");
                            if(!saveFile.exists()){
                                saveFile.getParentFile().mkdirs();
                                saveFile.createNewFile();
                            }
                            
                            YamlConfiguration saveData = new YamlConfiguration();
                            if(saveFile.exists()){
                                saveData = YamlConfiguration.loadConfiguration(saveFile);
                            }
                            
                            ConfigurationSection houseSection = saveData.createSection(houseName);
                            
                            houseSection.set(LetterBoxDataField.SIGN_LOCATION.toString(), serializeLocation(b.getLocation()));
                            houseSection.set(LetterBoxDataField.CONTAINER_LOCATION.toString(), serializeLocation(container.getLocation()));
                            houseSection.set(LetterBoxDataField.CONTAINER_TYPE.toString(), container.getType().toString());
                            
                            saveData.save(saveFile);
                            
                        } catch (
                                IOException e) {
                            throw new RuntimeException(e);
                        }*/
                        
                        
                    }
                }
            }
        }
    }
    
    public static String getLetterBoxIdentifier() {
        return signIdentifier;
    }
    
    enum LetterBoxDataField {
        SIGN_LOCATION,
        CONTAINER_LOCATION,
        CONTAINER_TYPE
        ;
        
        
        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT).replaceAll("_","-");
        }
    }
    
    protected static NamespacedKey getOwnerKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"letterbox-owner");
    }
    
    @EventHandler
    public static void checkForBrokenLetterboxIndirect(BlockDestroyEvent event){
        checkDestroyedBlock(event.getBlock());
    }
    
    @EventHandler
    public static void checkForBrokenLetterboxDirect(BlockBreakEvent event){
        checkDestroyedBlock(event.getBlock());
    }
    
    private static void checkDestroyedBlock(Block broken){
        if(broken.getState() instanceof Sign sign){
            List<Component> data = sign.lines();
            if(data.size() >= 3){
                final String identifier = ((TextComponent) data.get(0)).content();
                if(!identifier.equals(signIdentifier)) return;
                try{
                    final String idText = sign.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING);
                    if(idText != null){
                        final UUID id = UUID.fromString(idText);
                        final String houseName = ((TextComponent) data.get(2)).content();
                        
                        final File dataFile = new File(broken.getWorld().getWorldFolder().getCanonicalFile() + File.separator + "mailboxes" + File.separator + id + ".yml");
                        if(dataFile.exists()){
                            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
                            if(config.isConfigurationSection(houseName)){
                                config.set(houseName, null);
                                config.save(dataFile);
                                
                                if(loadedLetterboxes.containsKey(id)){
                                    Set<PostLetterBox> locs = loadedLetterboxes.getOrDefault(id,new HashSet<>());
                                    locs.remove(new PostLetterBox(id,sign.getBlock()));
                                    if(locs.size() > 0){
                                        loadedLetterboxes.put(id,locs);
                                    } else {
                                        loadedLetterboxes.remove(id);
                                    }
                                }
                            }
                        }
                    }
                    
                } catch (IllegalArgumentException | IOException e){e.printStackTrace();}
            }
        }
    }
    
    
    @EventHandler
    public static void loadLetterBoxes(PlayerJoinEvent event){
        loadPlayerLetterboxes(event.getPlayer().getUniqueId(),event.getPlayer().getWorld());
    }
    
    /**
     * Will try to load the player's letterboxes from save. Will do nothing if the player already has loaded letterboxes
     * @param playerId the UUID of the player you want to load
     * @param world the world for which letterboxes should be loaded
     */
    protected static void loadPlayerLetterboxes(UUID playerId, World world){
        if(!loadedLetterboxes.containsKey(playerId) || loadedLetterboxes.get(playerId).size() == 0){
            try{
                final File data = new File(world.getWorldFolder().getCanonicalFile() + File.separator + "mailboxes" + File.separator + playerId + ".yml");
                if(data.exists()){
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(data);
                    HashSet<PostLetterBox> boxes = new HashSet<>();
                    for(String s : config.getKeys(false)){
                        ConfigurationSection section = config.getConfigurationSection(s);
                        if(section != null){
                            final String serializes = section.getString(LetterBoxDataField.SIGN_LOCATION.toString());
                            if(serializes != null){
                                boxes.add(new PostLetterBox(playerId,Objects.requireNonNull(deserializeLocation(serializes)).getBlock()));
                            }
                        }
                    }
                    loadedLetterboxes.put(playerId,boxes);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    
    protected static String serializeLocation(Location loc){
        JSONObject data = new JSONObject(loc.serialize());
        return data.toJSONString();
    }
    
    protected static @Nullable Location deserializeLocation(String serialized){
        try {
            Object data = new JSONParser().parse(serialized);
            if(data instanceof Map<?, ?> map){
                Map<String, Object> output = new HashMap<>();
                for(Map.Entry<?, ?> entry : map.entrySet()){
                    if(entry.getKey() instanceof String v1 && entry.getValue() != null){
                        output.put(v1,entry.getValue());
                    }
                }
                return Location.deserialize(output);
            }
            
        } catch (
                ParseException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }
    
    
    //
    // INSTANTIATION DATA
    //
    private String name;
    private Location sign;
    private Location container;
    private Material containerMaterial;
    private Location platformLocation = null;
    private UUID owner;
    
    public PostLetterBox(UUID owner, Block sign, Location platformLocation) throws IllegalStateException, IndexOutOfBoundsException{
        if(sign.getState() instanceof Sign wallSign && sign.getBlockData() instanceof Directional directional){
            this.name = ((TextComponent)wallSign.line(2)).content();
            this.sign = sign.getLocation();
            this.owner = owner;
            final Block checkCont = sign.getRelative(directional.getFacing().getOppositeFace());
            if(!(checkCont.getState() instanceof Container)) {
                throw new IllegalStateException("The sign you provided cannot be used as a post sign (missing container)");
            }
            this.container = checkCont.getLocation();
            this.containerMaterial = checkCont.getType();
            
            
        } else {
            throw new IllegalStateException("The block provided is not a sign");
        }
    }
    
    /**
     * DO NOT USE THIS !! IT IS ONLY FOR EVENT REGISTRATION
     */
    public PostLetterBox(){}
    
    public PostLetterBox(UUID owner, Block sign){
        this(owner,sign,null);
    }
    
    public String getName() {
        return name;
    }
    
    public Location getSign() {
        return sign;
    }
    
    public Location getContainer() {
        return container;
    }
    
    public Material getContainerMaterial() {
        return containerMaterial;
    }
    
    public @Nullable Location getPlatformLocation() {
        return platformLocation;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPlatformLocation(Location platformLocation) {
        this.platformLocation = platformLocation;
    }
    
    protected void save(){
        try {
            final File saveFile = new File(sign.getWorld().getWorldFolder().getCanonicalFile()+File.separator+"mailboxes"+File.separator+owner+".yml");
            if(!saveFile.exists()){
                saveFile.getParentFile().mkdirs();
                saveFile.createNewFile();
            }
            
            
            YamlConfiguration saveData = new YamlConfiguration();
            if(saveFile.exists()){
                saveData = YamlConfiguration.loadConfiguration(saveFile);
            }
            
            ConfigurationSection houseSection = saveData.createSection(name);
            
            houseSection.set(LetterBoxDataField.SIGN_LOCATION.toString(), serializeLocation(sign));
            houseSection.set(LetterBoxDataField.CONTAINER_LOCATION.toString(), serializeLocation(container));
            houseSection.set(LetterBoxDataField.CONTAINER_TYPE.toString(), containerMaterial.toString());
            
            saveData.save(saveFile);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PostLetterBox box && this.sign.equals(box.sign) && this.name.equals(box.name) && this.owner.equals(box.owner);
    }
    
    @Override
    public int hashCode() {
        return (owner+":"+name+"@"+sign).hashCode();
    }
}
