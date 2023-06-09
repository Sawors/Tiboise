package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

public class PostEnvelope extends TiboiseItem {
    public PostEnvelope(){
        setMaterial(Material.PAPER);
        setHelpText("Right click on a lectern containing a book to transfer the content of the book to this envelope.");
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),new PostEnvelope().get());
        recipe.addIngredient(3,Material.PAPER);
        recipe.addIngredient(Material.INK_SAC);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
