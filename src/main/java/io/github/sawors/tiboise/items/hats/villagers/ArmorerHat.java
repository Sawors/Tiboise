package io.github.sawors.tiboise.items.hats.villagers;

import io.github.sawors.tiboise.items.TiboiseHat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class ArmorerHat extends TiboiseHat {
    
    public ArmorerHat(){
        setDefaultHatData();
        setMaterial(Material.WHEAT);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get())
                .shape(
                        "III",
                        "LIL"
                )
                .setIngredient('L',new ItemStack(Material.RABBIT_HIDE))
                .setIngredient('I',new ItemStack(Material.IRON_INGOT))
                ;
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
