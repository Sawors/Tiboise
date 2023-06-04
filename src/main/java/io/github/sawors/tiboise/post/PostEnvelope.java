package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

public class PostEnvelope extends TiboiseItem {
    public PostEnvelope(){
        setMaterial(Material.PAPER);
    }
    
    
    public static NamespacedKey getContentTextKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-text");
    }
    
    public static NamespacedKey getContentItemKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-item");
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),new PostEnvelope().get());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.INK_SAC);
        return recipe;
    }
}
