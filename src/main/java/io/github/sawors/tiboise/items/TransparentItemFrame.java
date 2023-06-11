package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.core.UtilityEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TransparentItemFrame extends TiboiseItem implements Listener, UtilityEntity {
    
    public TransparentItemFrame(){
        setMaterial(Material.ITEM_FRAME);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),new TransparentItemFrame().get())
                .shape(
                        "PPP",
                        "PIP",
                        "PPP"
                )
                .setIngredient('P',new ItemStack(Material.GLASS_PANE))
                .setIngredient('I', new ItemStack(Material.ITEM_FRAME))
                ;
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
    
    @EventHandler
    public void makeSpawnedTransparent(PlayerInteractEvent event){
        final Player p = event.getPlayer();
        ItemStack item = event.getItem();
        Block clicked = event.getClickedBlock();
        if(item != null && event.getAction().isRightClick() && clicked != null && getItemId(item).equals(getId(TransparentItemFrame.class))){
            event.setCancelled(true);
            item.setAmount(item.getAmount()-1);
            clicked.getWorld().spawnEntity(
                    clicked.getLocation().add(0,.5,0).add(event.getBlockFace().getDirection()),
                    EntityType.ITEM_FRAME,
                    CreatureSpawnEvent.SpawnReason.NATURAL,
                    entity -> {
                        ItemFrame frame = ((ItemFrame) entity);
                        frame.setFacingDirection(event.getBlockFace());
                        frame.setVisible(false);
                        frame.getPersistentDataContainer().set(utilityKey, PersistentDataType.STRING, getEntityIdentifier());
                    }
            );
            
        }
    }
    
    @EventHandler
    public void dropItemFrame(HangingBreakEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof ItemFrame && Objects.equals(entity.getPersistentDataContainer().get(utilityKey,PersistentDataType.STRING),getEntityIdentifier())){
            event.setCancelled(true);
            entity.getWorld().dropItem(entity.getLocation(),new TransparentItemFrame().get());
            entity.remove();
        }
    }
    
    @Override
    public String getEntityIdentifier() {
        return getId();
    }
}
