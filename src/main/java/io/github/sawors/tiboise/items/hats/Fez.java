package io.github.sawors.tiboise.items.hats;

import io.github.sawors.tiboise.items.TiboiseHat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class Fez extends TiboiseHat {
    public Fez(){
        setDefaultHatData();
        setMaterial(Material.RED_DYE);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get())
                .shape(
                        "XSX",
                        "LRL"
                )
                .setIngredient('S',new ItemStack(Material.STRING))
                .setIngredient('L',new ItemStack(Material.RABBIT_HIDE))
                .setIngredient('R',new ItemStack(Material.RED_DYE))
                ;
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
