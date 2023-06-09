package io.github.sawors.tiboise.items.hats;

import io.github.sawors.tiboise.items.TiboiseHat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class Sombrero extends TiboiseHat {
    public Sombrero(){
        setDefaultHatData();
        setMaterial(Material.GREEN_DYE);
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get())
                .shape(
                        "XCX",
                        "LLL"
                )
                .setIngredient('C',new ItemStack(Material.CACTUS))
                .setIngredient('L',new ItemStack(Material.RABBIT_HIDE))
                ;
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
