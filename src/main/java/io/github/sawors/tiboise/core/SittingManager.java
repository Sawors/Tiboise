package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
import io.papermc.paper.math.Rotations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Objects;

public class SittingManager implements Listener, UtilityEntity {
    
    @EventHandler
    public static void playerSit(PlayerInteractEvent event){
        final Block b = event.getClickedBlock();
        final Player p = event.getPlayer();
        if(
                b != null
                && (
                        (b.getBlockData() instanceof Stairs str
                            && str.getShape().equals(Stairs.Shape.STRAIGHT))
                            && str.getHalf().equals(Bisected.Half.BOTTOM)
                        ||
                        (b.getBlockData() instanceof Slab slb
                            && slb.getType().equals(Slab.Type.BOTTOM)
                        )
                )
                && !p.isSneaking()
                && event.getAction().isRightClick()
                && event.getHand() != null
                && event.getHand().equals(EquipmentSlot.HAND)
                && p.getInventory().getItemInMainHand().getType().equals(Material.AIR)
        ){
            if(p.isJumping() || p.isSwimming() || p.isFlying() || p.isClimbing() || p.isBlocking() || p.isRiptiding() || p.isSprinting()){
                p.sendActionBar(Component.text("you must be standing still to sit").color(NamedTextColor.RED));
            } else {
                // All conditions for player sitting matched
                final Block sideX1 = b.getRelative(1,0,0);
                final Block sideX2 = b.getRelative(-1,0,0);
                final Block sideZ1 = b.getRelative(0,0,1);
                final Block sideZ2 = b.getRelative(0,0,-1);
                Axis axis = null;
                if((sideX1.getBlockData() instanceof WallSign || sideX2.getBlockData() instanceof WallSign)){
                    axis = Axis.X;
                } else if ((sideZ1.getBlockData() instanceof WallSign || sideZ2.getBlockData() instanceof WallSign)){
                    axis = Axis.Z;
                }
                if(axis != null){
                    sitPlayerOnBlock(p,b,axis);
                }
            }
            
            
        }
    }
    
    
    
    public static void sitPlayerOnBlock(Player p, Block seat, Axis axis){
        if(!seat.getType().isSolid()) return;
        final Vector reference = new Vector(0,0,1);
        final Vector offset = seat.getBlockData() instanceof Stairs stairs ?
                stairs.getFacing().getDirection().multiply(-.20).add(new Vector(0,-0.5,0))
                : new Vector(0,0,0);
        final float yaw = seat.getBlockData() instanceof Stairs stairs
                ?
                (stairs.getFacing().getDirection().getX() < 0
                        ? -(float) Math.toDegrees(stairs.getFacing().getOppositeFace().getDirection().angle(reference))
                        : (float) Math.toDegrees(stairs.getFacing().getOppositeFace().getDirection().angle(reference)))
                : axis.equals(Axis.X) ? 0 : 90;
        //seat.getBoundingBox().getHeight()-0.95
        Location sitLoc = seat.getLocation();
        BoundingBox box = seat.getBoundingBox();
        sitLoc.setY(box.getCenterY());
        sitLoc.add(.5,(box.getHeight()/2)-0.95,.5);
        ArmorStand seatEntity = (ArmorStand) seat.getWorld().spawnEntity(
                sitLoc.add(offset),
                EntityType.ARMOR_STAND,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                e -> {
                    if(e instanceof ArmorStand armorStand){
                        armorStand.setInvisible(true);
                        armorStand.setGravity(false);
                        armorStand.setSmall(true);
                        armorStand.setArms(false);
                        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
                        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
                        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
                        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
                        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
                        armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
                        armorStand.getPersistentDataContainer().set(utilityKey, PersistentDataType.STRING, new SittingManager().getEntityIdentifier());
                        final Location location = armorStand.getLocation();
                        location.setYaw(yaw);
                        armorStand.teleport(location);
                        armorStand.setBodyYaw(yaw);
                        armorStand.setHeadRotations(Rotations.ofDegrees(0,yaw,0));
                    }
                }
                );
        final Location rotate = p.getLocation();
        rotate.setYaw(yaw);
        p.teleport(rotate);
        new BukkitRunnable(){
            @Override
            public void run() {
                seatEntity.addPassenger(p);
            }
        }.runTask(Tiboise.getPlugin());
    }
    
    @EventHandler
    public static void removeSeatEntityOnDismount(EntityDismountEvent event){
        if(event.getEntity() instanceof Player player
                && event.getDismounted() instanceof ArmorStand stand
                && Objects.equals(stand.getPersistentDataContainer().get(utilityKey,PersistentDataType.STRING),new SittingManager().getEntityIdentifier())
        ){
            stand.remove();
            player.teleport(player.getLocation().add(0,1,0));
        }
    }
    
    @EventHandler
    public static void removeSeatEntityOnServerStart(PluginEnableEvent event){
        final String identitfier = new SittingManager().getEntityIdentifier();
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            for(World world : Bukkit.getWorlds()){
                for(Entity e : world.getEntitiesByClass(ArmorStand.class)){
                    if(Objects.equals(e.getPersistentDataContainer().get(utilityKey,PersistentDataType.STRING),identitfier)){
                        e.remove();
                    }
                }
            }
        }
    }
    
    @Override
    public String getEntityIdentifier() {
        return "seat";
    }
}
