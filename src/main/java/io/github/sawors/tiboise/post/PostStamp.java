package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

public class PostStamp extends TiboiseItem {
    
    public PostStamp(){
        setMaterial(Material.PAPER);
        setVariant(formatTextToId(StampVariants.DEFAULT.toString()));
    }
    
    public PostStamp(StampVariants variant){
        this();
        this.setVariant(formatTextToId(variant.toString()));
    }
    
    
    
    public enum StampVariants {
        DEFAULT
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),new PostStamp().get());
        recipe.addIngredient(3,Material.PAPER);
        recipe.addIngredient(Material.PAPER);
        return recipe;
    }
}
