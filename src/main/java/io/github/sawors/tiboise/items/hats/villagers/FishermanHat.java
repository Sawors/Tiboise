package io.github.sawors.tiboise.items.hats.villagers;

import io.github.sawors.tiboise.items.TiboiseHat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class FishermanHat extends TiboiseHat {
    
    public FishermanHat(){
        setDefaultHatData();
        setMaterial(Material.WHEAT);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get())
                .shape(
                        "XWX",
                        "WWW"
                )
                .setIngredient('W',new ItemStack(Material.RABBIT_HIDE))
                ;
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
