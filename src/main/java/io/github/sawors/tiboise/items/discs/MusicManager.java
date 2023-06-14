package io.github.sawors.tiboise.items.discs;

import com.destroystokyo.paper.MaterialSetTag;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.UtilityEntity;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MusicManager implements Listener, UtilityEntity {
    
    private static final Map<ItemDisplay, Sound> discsPlaying = new HashMap<>();
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerUsesJukebox(PlayerInteractEvent event){
        if(event.getClickedBlock() != null && event.getAction().isRightClick() && event.getClickedBlock().getState() instanceof Jukebox jukebox){
            if(event.getHand() != null && event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                event.setCancelled(true);
                return;
            }
            final int DISK_ROTATION_PERIOD = 1;
            Block b = event.getClickedBlock();
            Player p = event.getPlayer();
            
            ItemStack disc;
            
            PlayerInventory pinv = p.getInventory();
            disc = pinv.getItemInMainHand();
            
            final boolean isTiboiseDisc = TiboiseItem.getItemId(disc).equals(TiboiseItem.getId(MusicDisc.class));
            
            // remove previous playing disc
            if(jukebox.isPlaying()){
                if((p.isSneaking() && disc.getType().equals(Material.AIR)) || (!p.isSneaking())){
                    for(ItemDisplay e : b.getLocation().add(.5,1,.5).getNearbyEntitiesByType(ItemDisplay.class,.1)){
                        if(Objects.equals(e.getPersistentDataContainer().get(utilityKey, PersistentDataType.STRING),getEntityIdentifier())){
                            e.remove();
                            if(discsPlaying.containsKey(e)){
                                for(Player p0 : e.getLocation().getNearbyEntitiesByType(Player.class,16*2)){
                                    p0.stopSound(discsPlaying.get(e).asStop());
                                }
                            }
                            if(p.getInventory().getItemInMainHand().getType().isAir()){
                                p.getInventory().setItemInMainHand(jukebox.getInventory().getRecord());
                                event.setCancelled(true);
                                jukebox.stopPlaying();
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        jukebox.setRecord(null);
                                        jukebox.getInventory().setRecord(null);
                                        jukebox.update();
                                    }
                                }.runTask(Tiboise.getPlugin());
                            }
                        }
                    }
                }
                
            } else if(MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(disc.getType()) ||isTiboiseDisc ){
                // play curent disc
                ItemDisplay display = (ItemDisplay) b.getWorld().spawnEntity(b.getLocation().add(.5,1+(.5/16),.5),EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM,
                        entity -> {
                            if(entity instanceof ItemDisplay itemDisplay){
                                itemDisplay.setItemStack(disc);
                                itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                                itemDisplay.getPersistentDataContainer().set(utilityKey,PersistentDataType.STRING,getEntityIdentifier());
                            }
                        }
                );
                
                event.setCancelled(true);
                
                p.getInventory().setItemInMainHand(disc.asQuantity(disc.getAmount()-1));
                
                final String baseKey = disc.getType().getKey().getKey();
                final int separatorIndex = disc.getType().getKey().getKey().lastIndexOf("_");
                
                // the maximum duration of the rotating animation. The first number is the minutes, the rest () are for the conversion in ticks
                int checkMusicDuration = 5*60;
                
                String musicData = null;
                if(isTiboiseDisc){
                    musicData = disc.getItemMeta().getPersistentDataContainer().get(MusicDisc.getMusicDataKey(),PersistentDataType.STRING);
                    checkMusicDuration = Integer.parseInt(Objects.requireNonNullElse(disc.getItemMeta().getPersistentDataContainer().get(MusicDisc.getDurationKey(),PersistentDataType.STRING),"300"))+1;
                }
                
                final NamespacedKey originalKey = new NamespacedKey(disc.getType().getKey().namespace(),baseKey.substring(0,separatorIndex)+"."+baseKey.substring(separatorIndex+1));
                NamespacedKey key = musicData != null ?  NamespacedKey.fromString(musicData) : originalKey;
                if(key == null) return;
                // difference of .5 in volume only to differentiate it from a vanilla sound
                final Sound music = Sound.sound(key, Sound.Source.RECORD,1.5f,1);
                final Sound baseSound = Sound.sound(originalKey, Sound.Source.RECORD,1,1);
                
                jukebox.setRecord(disc);
                jukebox.update();
                for(Player p2 : display.getLocation().getNearbyEntitiesByType(Player.class,32)){
                    p2.sendActionBar(Component.text(""));
                }
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for(Player p2 : display.getLocation().getNearbyEntitiesByType(Player.class,32)){
                            p2.stopSound(baseSound);
                        }
                        display.getWorld().playSound(music, display);
                        discsPlaying.put(display,music);
                        
                    }
                }.runTaskLater(Tiboise.getPlugin(),2);
                
                final int maxParticleDuration = checkMusicDuration*2;
                new BukkitRunnable(){
                    
                    int timer = maxParticleDuration;
                    
                    @Override
                    public void run() {
                        
                        if(display.isValid() && timer > 0){
                            display.getWorld().spawnParticle(Particle.NOTE,display.getLocation().add(0,0.2,0),1,.1,.1,.1,Math.random());
                            timer--;
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Tiboise.getPlugin(),1,10);
                
                // rotations per minute
                double speed = 1;
                switch (b.getRelative(BlockFace.DOWN).getType()){
                    case COPPER_BLOCK -> speed=1.125;
                    case IRON_BLOCK -> speed=2.25;
                    case GOLD_BLOCK -> speed=4.5;
                }
                
                final double rpm = speed;
                final int maxMusicDuration = checkMusicDuration*20;
                new BukkitRunnable(){
                    
                    
                    int timer = maxMusicDuration;
                    final Location blockloc = b.getLocation().clone();
                    final double step = ((rpm*60.0)/(20*DISK_ROTATION_PERIOD));
                    @Override
                    public void run(){
                        
                        if(timer <= 0){
                            this.cancel();
                        } else{
                            if(blockloc.getBlock().getType() != Material.JUKEBOX){
                                Sound sound = discsPlaying.get(display);
                                if(sound != null){
                                    for(Player p : display.getLocation().getNearbyEntitiesByType(Player.class,sound.volume()*32)){
                                        p.stopSound(sound);
                                    }
                                }
                                display.remove();
                                this.cancel();
                                return;
                            }
                            display.setRotation((float) (display.getLocation().getYaw()+step), 0);
                            timer--;
                            
                        }
                    }
                    
                }.runTaskTimer(Tiboise.getPlugin(), 0, DISK_ROTATION_PERIOD);
                
            }
            
            
            
            
        }
    }
    
    @EventHandler
    public void onPlayerBreakJukebox(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.JUKEBOX){
            for(Entity e : event.getBlock().getLocation().add(0.5,1,0.5).getNearbyEntities(0.1,0.1,0.1)){
                if(
                        e instanceof ItemDisplay display
                        && display.getItemStack() != null
                        && MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(display.getItemStack().getType())
                        && display.getPersistentDataContainer().has(utilityKey)
                        && Objects.equals(display.getPersistentDataContainer().get(utilityKey, PersistentDataType.STRING), getEntityIdentifier())
                ){
                    Sound sound = discsPlaying.get(display);
                    if(sound != null){
                        for(Player p : display.getLocation().getNearbyEntitiesByType(Player.class,sound.volume()*32)){
                            p.stopSound(sound);
                        }
                    }
                    e.remove();
                }
            }
        }
    }
    
    
    @Override
    public String getEntityIdentifier() {
        return "disc-holder";
    }
    
    
}
