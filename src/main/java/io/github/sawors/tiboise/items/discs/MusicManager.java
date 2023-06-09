package io.github.sawors.tiboise.items.discs;

import com.destroystokyo.paper.MaterialSetTag;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.UtilityEntity;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MusicManager extends UtilityEntity implements Listener {
    
    private static final Map<ItemDisplay, Sound> discsPlaying = new HashMap<>();
    
    @EventHandler
    public void onPlayerUsesJukebox(PlayerInteractEvent event){
        if(event.getClickedBlock() != null && event.getAction().isRightClick() && event.getClickedBlock().getState() instanceof Jukebox jukebox){
            final int DISK_ROTATION_PERIOD = 1;
            Block b = event.getClickedBlock();
            Location uploc = b.getLocation().clone().add(0.5,2,0.5);
            Player p = event.getPlayer();
            
            ItemStack disc;
            
            PlayerInventory pinv = p.getInventory();
            if(MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(pinv.getItemInMainHand().getType())){
                disc = pinv.getItemInMainHand().clone();
            } else if(MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(pinv.getItemInOffHand().getType())){
                disc = pinv.getItemInOffHand().clone();
            } else {
                disc = null;
            }
            
            // remove previous playing disc
            if(jukebox.isPlaying()){
                if((p.isSneaking() && p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) || (!p.isSneaking())){
                    for(ItemDisplay e : b.getLocation().add(.5,1,.5).getNearbyEntitiesByType(ItemDisplay.class,.5)){
                        if(Objects.equals(e.getPersistentDataContainer().get(getUtilityEntityKey(), PersistentDataType.STRING),getEntityIdentifier())){
                            e.remove();
                        }
                        /*if(e instanceof ArmorStand && ((ArmorStand) e).getEquipment().getHelmet() != null && ((ArmorStand) e).getEquipment().getHelmet().getType().toString().contains("MUSIC_DISC")){
                            e.remove();
                        }*/
                    }
                }
                
            } else if(disc != null && MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(disc.getType())){
                // play curent disc
                ItemDisplay display = (ItemDisplay) b.getWorld().spawnEntity(b.getLocation().add(.5,1+(.5/16),.5),EntityType.ITEM_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM,
                        entity -> {
                            if(entity instanceof ItemDisplay itemDisplay){
                                itemDisplay.setItemStack(disc);
                                itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                                itemDisplay.getPersistentDataContainer().set(getUtilityEntityKey(),PersistentDataType.STRING,getEntityIdentifier());
                            }
                        }
                );
                
                //final String musicName = disk.getType().getKey()
                event.setCancelled(true);
                
                final String baseKey = disc.getType().getKey().getKey();
                final int separatorIndex = disc.getType().getKey().getKey().lastIndexOf("_");
                
                NamespacedKey key = new NamespacedKey(disc.getType().getKey().namespace(),baseKey.substring(0,separatorIndex)+"."+baseKey.substring(separatorIndex+1));
                final Sound music = Sound.sound(key, Sound.Source.RECORD,2,1);
                display.getWorld().playSound(music, display);
                
                jukebox.getInventory().setRecord(disc);
                jukebox.stopPlaying();
                /*ArmorStand display = (ArmorStand) uploc.getWorld().spawnEntity(uploc.subtract(0,17/16f,0), EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM);
                display.setVisible(false);
                display.setInvulnerable(true);
                display.setGravity(false);
                display.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
                display.getEquipment().setHelmet(disk);
                display.setCustomNameVisible(false);
                display.customName(Component.text("_display"));
                display.setSmall(true);*/
                new BukkitRunnable(){
                    
                    final int MAX_MUSIC_DURATION = 5 *(20*60);
                    int timer = MAX_MUSIC_DURATION;
                    final Location blockloc = b.getLocation().clone();
                    final float step = (40f/(20*DISK_ROTATION_PERIOD));
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
                            display.setRotation(display.getLocation().getYaw()+step, 0);
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
                        && display.getPersistentDataContainer().has(getUtilityEntityKey())
                        && Objects.equals(display.getPersistentDataContainer().get(getUtilityEntityKey(), PersistentDataType.STRING), getEntityIdentifier())
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
