package io.github.sawors.tiboise.items.armor;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class CottonBoots extends TiboiseItem implements Listener {
    
    public CottonBoots() {
        setMaterial(Material.LEATHER_BOOTS);
        setHelpText("Prevent fall damage");
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(this.getItemKey(),new CottonBoots().get())
                .shape("WLW")
                .setIngredient('W', new RecipeChoice.MaterialChoice(Tag.WOOL))
                .setIngredient('L', Material.LEATHER_BOOTS);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
    
    @EventHandler
    public void preventFallDamage(EntityDamageEvent event){
        Entity e = event.getEntity();
        if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && e instanceof LivingEntity living && living.getEquipment() != null && living.getEquipment().getBoots() != null && getItemId(living.getEquipment().getBoots()).equals(getId(CottonBoots.class))){
            living.getEquipment().getBoots().damage((int) event.getFinalDamage(),living);
            event.setDamage(0);
        }
    }
}
