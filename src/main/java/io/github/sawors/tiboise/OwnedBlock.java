package io.github.sawors.tiboise;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class OwnedBlock implements Listener {
    public static final NamespacedKey ownerKey = new NamespacedKey(Tiboise.getPlugin(),"block-owner");
    
    public abstract UUID getOwner();
    
    @EventHandler
    public static void preventNotOwnerFromDestroying(BlockBreakEvent event){
        if(!event.isCancelled() && event.getBlock().getState() instanceof PersistentDataHolder holder){
            final String ownerId = holder.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
            // prevents other people from destroying this block
            if(!event.getPlayer().isOp() && ownerId != null && !ownerId.equals(event.getPlayer().getUniqueId().toString())) {
                event.getPlayer().sendActionBar(Component.text("You are not the owner of this block").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public static void preventNotOwnerFromInteracting(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        if(b!=null && b.getState() instanceof PersistentDataHolder holder){
            final String ownerId = holder.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
            // prevents other people from interacting with this block
            if(!event.getPlayer().isOp() && ownerId != null && !ownerId.equals(event.getPlayer().getUniqueId().toString())) {
                event.getPlayer().sendActionBar(Component.text("You are not the owner of this block").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }
    
    public static void setOwnership(Block b, @Nullable UUID owner) {
        BlockState state = b.getState(false);
        if(state instanceof PersistentDataHolder holder){
            if(owner == null){
                holder.getPersistentDataContainer().remove(ownerKey);
                state.update();
            } else {
                holder.getPersistentDataContainer().set(ownerKey,PersistentDataType.STRING,owner.toString());
                state.update();
            }
        }
    }
    
    public static @Nullable UUID getOwnership(@NotNull Block block){
        UUID id;
        BlockState state = block.getState(true);
        if(state instanceof PersistentDataHolder holder){
            String idS = holder.getPersistentDataContainer().getOrDefault(ownerKey,PersistentDataType.STRING,"");
            if(idS.length() > 1){
                try{
                    id = UUID.fromString(idS);
                    return id;
                } catch (IllegalArgumentException e){
                    return null;
                }
            }
        }
        return null;
    }
}
