package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

public class PostEnvelope extends TiboiseItem {
    public PostEnvelope(){
        setMaterial(Material.PAPER);
        
    }
    
    @Override
    public String getHelpText() {
        return "To fill this envelope, put it in a crafting grid with a completed stamp and a written book. \n Write your message in the book. You can use as many pages as you want.";
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),new PostEnvelope().get());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.INK_SAC);
        return recipe;
    }
}
