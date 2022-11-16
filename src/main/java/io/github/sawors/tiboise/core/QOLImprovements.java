package io.github.sawors.tiboise.core;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.github.sawors.tiboise.Tiboise;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chain;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class QOLImprovements implements Listener {
    
    //
    //  COMPASS NORTH
    @EventHandler
    public void setCompassNorth(PlayerChangedWorldEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Player p = event.getPlayer();
                p.setCompassTarget(new Location(p.getWorld(), 0,0,-1000000));
                p.updateInventory();
            }
        }.runTask(Tiboise.getPlugin());
        
    }
    @EventHandler
    public void setCompassNorth(PlayerJoinEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Player p = event.getPlayer();
                p.setCompassTarget(new Location(p.getWorld(), 0,0,-1000000));
                p.updateInventory();
            }
        }.runTask(Tiboise.getPlugin());
    }
    //
    //  DISABLE RESPAWN ANCHORS
    @EventHandler(priority = EventPriority.LOW)
    public void disableRespawnAnchors(PlayerInteractEvent event){
        if(event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.RESPAWN_ANCHOR) && event.getAction().isRightClick() && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GLOWSTONE)){
            event.setCancelled(true);
        }
    }
    //
    //  CHAIN CLIMB
    @EventHandler
    public void chainClimber(PlayerInteractEvent event){
        Player p = event.getPlayer();
        
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHAIN && p.getLocation().add(0,1,0).getBlock().getType().equals(Material.CHAIN) && ((Chain) p.getLocation().add(0,1,0).getBlock().getBlockData()).getAxis().equals(Axis.Y)){
            if(p.isSneaking()){
                p.setVelocity(new Vector(0,.25,0));
                p.getWorld().playSound(p.getLocation().add(0,1,0), Sound.BLOCK_CHAIN_STEP, .25f, 1);
            }else{
                p.setVelocity(new Vector(0,.33,0));
                p.getWorld().playSound(p.getLocation().add(0,1,0), Sound.BLOCK_CHAIN_STEP, 1, 1);
            }
        }
    }
    //
    //  OPEN DOUBLE DOORS
    @EventHandler
    public void onPlayerOpenDoor(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        if(b != null && b.getBlockData() instanceof Door door && event.getAction().isRightClick()){
            //event.setCancelled(true);
            
            
            
            //DOUBLE DOOR LOGIC
            if(!event.useInteractedBlock().equals(Event.Result.DENY) && !event.getPlayer().isSneaking()) {
                Block b1;
                Block b2;
                if (door.getFacing().equals(BlockFace.NORTH) || door.getFacing().equals(BlockFace.SOUTH)) {
                    b1 = b.getLocation().add(1, 0, 0).getBlock();
                    b2 = b.getLocation().add(-1, 0, 0).getBlock();
                    
                    
                } else {
                    b1 = b.getLocation().add(0, 0, 1).getBlock();
                    b2 = b.getLocation().add(0, 0, -1).getBlock();
                }
                if (b1.getBlockData() instanceof Door d1 && d1.getHinge() != door.getHinge()) {
                    if (door.isOpen()) {
                        d1.setOpen(false);
                        b1.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
                    } else {
                        d1.setOpen(true);
                        b1.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_OPEN, 1, 1);
                    }
                    b1.setBlockData(d1);
                    b1.getState().update();
                    
                }
                if (b2.getBlockData() instanceof Door d2 && d2.getHinge() != door.getHinge()) {
                    if (door.isOpen()) {
                        d2.setOpen(false);
                        b2.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
                    } else {
                        d2.setOpen(true);
                        b2.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_OPEN, 1, 1);
                    }
                    b2.setBlockData(d2);
                    b2.getState().update();
                }
            }
        }
        if(b != null && b.getType().toString().contains("_DOOR") && event.getAction().isLeftClick() && event.getPlayer().getInventory().getItemInMainHand().getType().isAir()){
            if(event.getPlayer().isSneaking()){
                b.getWorld().playSound(b.getLocation().add(.5,0,.5), "minecraft:sawors.door.knock", .25f, randomPitchSimple()-0.5f);
            }else{
                b.getWorld().playSound(b.getLocation().add(.5,0,.5), "minecraft:sawors.door.knock", 1, randomPitchSimple());
            }
        }
    }
    //
    //  TORCH BURN BLOCKS WHEN DROPPED
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event){
        if(event.getEntity().getItemStack().getType() == Material.TORCH && Math.random() < 0.5){
            event.getEntity().getLocation().getBlock().setType(Material.FIRE);
        }
    }
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event){
        if(event.getEntity().getItemStack().getType() == Material.TORCH){
            event.getEntity().setTicksLived(4800);
        }
    }
    //
    //  ARROW SOUND + BREAK BLOCKS
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event){
        if(event.getHitBlock() != null && event.getEntity() instanceof Arrow arrow){
            Block block = event.getHitBlock();
            if(block.getType().getBlastResistance() > 1){
                if(Math.random() < block.getType().getBlastResistance()/10){
                    arrow.getWorld().spawnParticle(Particle.ITEM_CRACK, arrow.getLocation(),2,.1,.1,.1,.1,new ItemStack(Material.STICK));
                    arrow.getWorld().spawnParticle(Particle.ITEM_CRACK, arrow.getLocation(),4,.1,.1,.1,.1,new ItemStack(Material.IRON_INGOT));
                    block.getWorld().playSound(block.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, randomPitchSimple()+2f);
                    arrow.remove();
                } else {
                    block.getWorld().playSound(block.getLocation(), block.getBlockSoundGroup().getPlaceSound(), 1f, randomPitchSimple()+0.5f);
                }
            } else {
                arrow.getWorld().spawnParticle(Particle.BLOCK_CRACK, arrow.getLocation(),6,.1,.1,.1,.1,block.getBlockData());
                block.getWorld().playSound(block.getLocation(), block.getBlockSoundGroup().getPlaceSound(), 1f, randomPitchSimple()+0.2f);
            }
            
            // switch for sound
            
            
            
            //arrow break reaction
            if(block.getType().toString().contains("GLASS_PANE")){
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1,1.2f);
                        block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getX()+0.5, block.getY()+0.5, block.getZ()+0.5,16, 0, 0,0, block.getBlockData());
                        block.getWorld().spawnParticle(Particle.FLAME, block.getLocation(), 1);
                        block.breakNaturally();
                    }
                }.runTaskLater(Tiboise.getPlugin(), 1);
            }
        }
    }
    
    //
    // TORCH PUT THE TARGET ON FIRE
    @EventHandler
    public static void setEntityOnFireWhenHitWithTorch(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player p && Objects.equals(p.getInventory().getItemInMainHand().getType(),Material.TORCH)){
            event.getEntity().setFireTicks(20*2);
        }
    }
    
    //
    //  REMOVE XP
    @EventHandler
    public void onXpSpawn(EntityAddToWorldEvent event){
        if(event.getEntity() instanceof ExperienceOrb){
            new BukkitRunnable(){
                @Override
                public void run() {
                    event.getEntity().remove();
                }
            }.runTask(Tiboise.getPlugin());
        }
    }
    @EventHandler
    public static void removeXpEarning(PlayerExpChangeEvent event){
        event.setAmount(0);
    }
    
    //
    // SUBTLE LEAVE MESSAGES
    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        if(event.quitMessage() != null){
            event.quitMessage(Component.text(ChatColor.GRAY+"← "+event.getPlayer().getName()));
        }
    }
    
    //
    // SUBTLE JOIN MESSAGES
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(event.joinMessage() != null){
            event.joinMessage(Component.text(ChatColor.GRAY+"→ "+event.getPlayer().getName()));
        }
    }
    
    /**
     * Used to apply some subtle random pitch variations to a sound
     * @return      a random pitch between 0.8 and 1.2 (float)
     */
    public static float randomPitchSimple(){
        return (float) ((new Random().nextDouble() * 0.4) + 0.8);
    }
    
    @EventHandler
    public static void onLoad(PluginEnableEvent event){
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            List<Recipe> vanillabonusrecipes = new ArrayList<>();
            
            // SADDLES
            vanillabonusrecipes.add(
                    new ShapedRecipe(new NamespacedKey(Key.MINECRAFT_NAMESPACE,new ItemStack(Material.SADDLE).getType().toString().toLowerCase(Locale.ROOT)), new ItemStack(Material.SADDLE)).shape(
                            "LLL",
                            "SXS"
                    )
                    .setIngredient('X', Material.AIR)
                    .setIngredient('L', Material.LEATHER)
                    .setIngredient('S', Material.STRING)
            );
            // NAME TAGS
            vanillabonusrecipes.add(
                    new ShapelessRecipe(new NamespacedKey(Key.MINECRAFT_NAMESPACE,new ItemStack(Material.NAME_TAG).getType().toString().toLowerCase(Locale.ROOT)), new ItemStack(Material.NAME_TAG))
                    .addIngredient(Material.GOLD_NUGGET)
                    .addIngredient(Material.PAPER)
            );
            
            
            for(Recipe r : vanillabonusrecipes){
                Bukkit.addRecipe(r);
            }
        }
    }
}
