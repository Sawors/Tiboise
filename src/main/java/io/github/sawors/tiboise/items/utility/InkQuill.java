package io.github.sawors.tiboise.items.utility;

import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class InkQuill extends TiboiseItem implements Listener {
    
    public InkQuill(){
        setMaterial(Material.FEATHER);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemKey(),this.get());
        recipe.addIngredient(new ItemStack(Material.FEATHER));
        recipe.addIngredient(new ItemStack(Material.INK_SAC));
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void editSigns(PlayerInteractEvent event){
        if(!event.useInteractedBlock().equals(Event.Result.DENY) && event.getClickedBlock() != null && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND) && getItemId(event.getPlayer().getInventory().getItemInMainHand()).equals(getId(InkQuill.class))){
            final Block b = event.getClickedBlock();
            if(b.getState() instanceof Sign sign){
                event.getPlayer().openSign(sign);
            }
        }
    }
    
    @EventHandler
    public static void editSignSound(SignChangeEvent event){
        final Player p = event.getPlayer();
        if(getItemId(p.getInventory().getItemInMainHand()).equals(getId(InkQuill.class))){
            p.getWorld().playSound(p.getLocation(),Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER,.5f,1);
        }
    }
}
