package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.core.ItemTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TiboiseWrench extends TiboiseItem implements Listener {
    
    public TiboiseWrench(){
        setMaterial(Material.STICK);
        setId("wrench");
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setDisplayName(Component.text("Wrench").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setLore(List.of(
                Component.text(ChatColor.GRAY+"This wrench is able to modify block states,"),
                Component.text(ChatColor.GRAY+"Usually this consist in a rotation")
        ));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseWrench(PlayerInteractEvent event){
        // TODO : add worldguard support for this
        Block b = event.getClickedBlock();
        boolean sneaking = event.getPlayer().isSneaking();
        if(b != null && Objects.equals(event.getHand(), EquipmentSlot.HAND) && !event.useInteractedBlock().equals(Event.Result.DENY) && TiboiseItem.getItemId(event.getPlayer().getInventory().getItemInMainHand()).equals(TiboiseItem.getItemId(new TiboiseWrench().get()))){
            
            BlockData data = b.getBlockData();
            final BlockData reference = data.clone();
            
            if(data instanceof Orientable or){
                // rotate along X/Y/Z axes if not sneaking
                Axis axis = or.getAxis();
                if(!sneaking){
                    if(axis.equals(Axis.X)){
                        axis = Axis.Z;
                    } else if(axis.equals(Axis.Z)){
                        axis = Axis.X;
                    }
                }
                // invert rotation axis if sneaked (X/Z -> Y, Y -> X)
                else {
                    if((axis.equals(Axis.X) || axis.equals(Axis.Z)) && or.getAxes().contains(Axis.Y)) {
                        axis = Axis.Y;
                    } else if(or.getAxes().contains(Axis.X)){
                        axis = Axis.X;
                    }
                }
                or.setAxis(axis);
            } /*else if(data instanceof Fence fc){
                // if already connected : disconnect
                if(fc.getFaces().size() >= 1){
                    for(BlockFace face : fc.getFaces()){
                        fc.setFace(face, false);
                    }
                }
                // if disconnected : try to connect
                else {
                    Main.logAdmin(fc.getMaterial());
                    for(BlockFace face : fc.getAllowedFaces()){
                        Block relative = b.getRelative(face);
                        if(relative.getBlockData() instanceof Fence f2){
                            final Fence b2dt = (Fence) f2.clone();
                            fc.setFace(face,true);
                            
                            if(sneaking){
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        for(BlockFace face1 : b2dt.getAllowedFaces()){
                                            b2dt.setFace(face1,false);
                                            Main.logAdmin("hehehehe");
                                        }
                                        relative.setBlockData(b2dt);
                                    }
                                }.runTaskLater(Main.getPlugin(),60);
                            }
                            
                        }
                    }
                }
            }*/
            else if(data instanceof Directional dr){
                if(!sneaking){
                    BlockFace facing = dr.getFacing();
                    if(facing.equals(BlockFace.UP)){
                        dr.setFacing(BlockFace.DOWN);
                    } else if (facing.equals(BlockFace.DOWN)) {
                        dr.setFacing(BlockFace.UP);
                    } else if(!facing.equals(BlockFace.SELF)){
                        boolean halfRotation = dr.getFaces().contains(BlockFace.NORTH_EAST);
                        boolean quarterRotation = dr.getFaces().contains(BlockFace.NORTH_NORTH_EAST);
                        int step = halfRotation ? 2 : 4;
                        step = quarterRotation ? 1 : step;
        
                        List<BlockFace> possibleFaces = TiboiseUtils.getSortedHorizontalBlockFaces();
                        int index = possibleFaces.indexOf(facing);
                        if(index < 0) return;
                        index = (index+step) % possibleFaces.size();
                        BlockFace endFace = possibleFaces.get(index);
                        if (dr.getFaces().contains(endFace)) {
                            dr.setFacing(endFace);
                        }
                    }
                } else if(data instanceof Stairs str){
                    str.setHalf(str.getHalf().equals(Bisected.Half.TOP) ? Bisected.Half.BOTTOM : Bisected.Half.TOP);
                } else if(data instanceof Door door){
                    door.setHinge(door.getHinge() == Door.Hinge.LEFT ? Door.Hinge.RIGHT : Door.Hinge.LEFT);
                }
                if(b.getType().equals(Material.IRON_TRAPDOOR) && sneaking){
                    Player p = event.getPlayer();
                    if(data instanceof Openable openable && data instanceof Powerable pw && !pw.isPowered()){
                        if(TiboiseUtils.consumeItemInInventory(p.getInventory(),new ItemStack(Material.REDSTONE)) ){
                            openable.setOpen(!openable.isOpen());
                            b.getWorld().playSound(b.getLocation(), !openable.isOpen() ? Sound.BLOCK_IRON_TRAPDOOR_CLOSE : Sound.BLOCK_IRON_TRAPDOOR_OPEN,1,1);
                        } else {
                            p.sendActionBar(Component.text(ChatColor.RED+"You must have "+ChatColor.BOLD+"redstone dust"+ChatColor.RED+" in your inventory in order to switch the "+data.getMaterial().toString().toLowerCase(Locale.ROOT).replaceAll("_"," ")));
                        }
                    }
                }
            }
            
            
            if(!data.equals(reference)){
                b.setBlockData(data);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        event.getPlayer().closeInventory();
                    }
                }.runTask(Main.getPlugin());
                b.getWorld().playSound(b.getLocation(),b.getType().createBlockData().getSoundGroup().getPlaceSound(),1,1);
                b.getWorld().spawnParticle(Particle.BLOCK_DUST,b.getLocation().add(.5,.5,.5),8,.5,.5,.5,.1,b.getBlockData());
            }
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        return new ShapedRecipe(getIdAsKey(),this.get()).shape("XIX","XII","IXX").setIngredient('X',Material.AIR).setIngredient('I',Material.IRON_INGOT);
    }
}
