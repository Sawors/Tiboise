package io.github.sawors.tiboise.items.hats;

import io.github.sawors.tiboise.items.TiboiseHat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class Monocle extends TiboiseHat {
    public Monocle(){
        setDefaultHatData();
        setMaterial(Material.GOLD_NUGGET);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get())
                .shape(
                        "GGG",
                        "GPG",
                        "XXG"
                )
                .setIngredient('G',new ItemStack(Material.GOLD_BLOCK))
                .setIngredient('P',new ItemStack(Material.GLASS_PANE))
                ;
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
