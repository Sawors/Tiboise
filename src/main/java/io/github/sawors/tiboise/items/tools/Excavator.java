package io.github.sawors.tiboise.items.tools;

import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.core.ItemVariant;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Excavator extends RadiusBreakingTool implements Listener {
    
    private static final double durabilitymultiplier = 1.75;
    
    /**
     * NEVER USE THIS ALONE IF IT IS TO CREATE AN ITEM
     */
    public Excavator(){
    }
    
    public Excavator(ItemVariant variant){
        setVariant(variant.toString());
    }
    
    @Override
    public void setVariant(String variant) {
        super.setVariant(variant);
        String varname = variant.toLowerCase(Locale.ROOT);
        setDisplayName(Component.text(Main.getUpperCamelCase(Character.toUpperCase(varname.charAt(0))+varname.substring(1)+" "+getId())).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        try{
            switch (ItemVariant.valueOf(variant.toUpperCase(Locale.ROOT))){
                case STONE -> {setMaterial(Material.STONE_SHOVEL);}
                case WOOD -> {setMaterial(Material.WOODEN_SHOVEL);}
                case IRON -> {setMaterial(Material.IRON_SHOVEL);}
                case GOLD -> {setMaterial(Material.GOLDEN_SHOVEL);}
                case DIAMOND -> {setMaterial(Material.DIAMOND_SHOVEL);}
                case NETHERITE, STEEL -> {setMaterial(Material.NETHERITE_SHOVEL);}
            }
        } catch (IllegalArgumentException e){
            setMaterial(Material.WOODEN_SHOVEL);
        }
    }
    
    @EventHandler
    public static void onPlayerDestroyBlock(BlockBreakEvent event){
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Block target = event.getBlock();
        if(target.isValidTool(item) && TiboiseItem.getItemId(item).equals(new Excavator().getId()) && !event.getPlayer().isSneaking()){
            radiusBreak(RadiusType.SQUARE,1,event.getBlock(), Objects.requireNonNull(event.getPlayer().getTargetBlockFace(8)),event.getPlayer().getInventory().getItemInMainHand(),durabilitymultiplier);
        }
    }
    
    @Override
    public @Nullable ShapedRecipe getRecipe(ItemVariant variant) {
        if(!getPossibleVariants().contains(variant)){
            return null;
        }
        
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Main.getPlugin(),variant.toString().toLowerCase(Locale.ROOT)+"_"+getId()),new Excavator(variant).get());
        recipe.shape(
                "MMM",
                "MSM",
                "XSX"
        );
        
        Material mat = Material.AIR;
        RecipeChoice choices = null;
        switch(variant){
            case WOOD -> choices = new RecipeChoice.MaterialChoice(Tag.PLANKS);
            case STONE -> choices = new RecipeChoice.MaterialChoice(Tag.ITEMS_STONE_TOOL_MATERIALS);
            case IRON -> mat = Material.IRON_INGOT;
            case GOLD -> mat = Material.GOLD_INGOT;
            case DIAMOND -> mat = Material.DIAMOND;
            case NETHERITE, STEEL -> mat = Material.NETHERITE_INGOT;
        }
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('X', Material.AIR);
        if(choices != null){
            recipe.setIngredient('M', choices);
        } else {
            recipe.setIngredient('M', mat);
        }
        return recipe;
    }
    
    @Override
    public List<ItemVariant> getPossibleVariants(){
        return List.of(
                ItemVariant.STONE,
                ItemVariant.IRON,
                ItemVariant.GOLD,
                ItemVariant.DIAMOND,
                ItemVariant.NETHERITE
        );
    }
}
