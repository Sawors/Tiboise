package io.github.sawors.tiboise.items.armor.scuba;

import io.github.sawors.tiboise.items.DurabilityItem;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import javax.annotation.Nullable;

public class DivingChestplate extends TiboiseItem implements Listener, DurabilityItem {
    
    public DivingChestplate() {
        setMaterial(Material.CHAINMAIL_CHESTPLATE);
        setDisplayName(Component.text("Diving Chestplate").color(NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setShortLore("Grants you better protection against underwater menaces");
        addTag(ItemTag.PREVENT_BREAKING);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
    }
    
    
    @EventHandler
    public static void scubaChestplateEffect(EntityDamageByEntityEvent event) {
        final String refId = getId(DivingChestplate.class);
        final Entity e = event.getEntity();
        final Entity attacker = event.getDamager();
        if (e instanceof LivingEntity livingEntity && livingEntity.getEquipment() != null && getItemId(livingEntity.getEquipment().getChestplate()).equals(refId) && livingEntity.getEquipment().getChestplate().getItemMeta() instanceof Damageable dmg && dmg.getDamage() < livingEntity.getEquipment().getChestplate().getType().getMaxDurability()-1) {
            if(attacker instanceof Drowned || attacker instanceof Guardian){
                final double damage = event.getDamage()/2.0;
                event.setDamage(event.getDamage());
                livingEntity.getEquipment().getChestplate().damage((int) damage, livingEntity);
            } else if (attacker instanceof PufferFish){
                event.setCancelled(true);
            }
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get());
        recipe.shape(
                "CXC",
                "CNC",
                "CCC"
        );
        recipe.setIngredient('C',Material.COPPER_INGOT);
        recipe.setIngredient('N',Material.NAUTILUS_SHELL);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
    
    @Override
    public String getRepairMaterialId() {
        return getItemId(new ItemStack(Material.COPPER_INGOT));
    }
    
    @Override
    public int getRepairPointPerItem() {
        return 35;
    }
}
