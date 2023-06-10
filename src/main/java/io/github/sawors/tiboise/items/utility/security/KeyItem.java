package io.github.sawors.tiboise.items.utility.security;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KeyItem extends IdentifiedItem{
    
    final static String defaultIdentifier = "000000";
    final static int identifierLength = 6;
    
    public KeyItem() {
        setMaterial(Material.IRON_NUGGET);
        setDisplayName(Component.text("Key").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setIdentifier(defaultIdentifier);
    }
    
    @Override
    public void setIdentifier(String identifier) {
        super.setIdentifier(identifier);
        setLore(List.of(Component.text(identifier).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get())
                .shape("N","I")
                .setIngredient('I',new ItemStack(Material.IRON_INGOT))
                .setIngredient('N',new ItemStack(Material.IRON_NUGGET))
                ;
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
    
    @Override
    public ItemStack get() {
        if(getIdentifier().equals(defaultIdentifier)){
            setIdentifier(RandomStringUtils.randomAlphanumeric(identifierLength));
        }
        return super.get();
    }
}
