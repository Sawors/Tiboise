package io.github.sawors.tiboise.items.tools.radius;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.ItemVariant;
import io.github.sawors.tiboise.items.DurabilityItem;
import io.github.sawors.tiboise.items.ItemTag;
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
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Hammer extends RadiusBreakingTool implements Listener, DurabilityItem {
    
    private static final double durabilitymultiplier = 2;
    
    /**
     * NEVER USE THIS ALONE IF IT IS TO CREATE AN ITEM
     */
    public Hammer(){
    }
    
    public Hammer(ItemVariant variant){
        setVariant(variant.toString());
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
    }
    
    @Override
    public void setVariant(String variant) {
        super.setVariant(variant);
        String varname = variant.toLowerCase(Locale.ROOT);
        String append = variant.equalsIgnoreCase("gold") || variant.equalsIgnoreCase("wood") ? "en" : "";
        setDisplayName(Component.text(Tiboise.getUpperCamelCase(Character.toUpperCase(varname.charAt(0))+varname.substring(1)+append+" "+getId())).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        try{
            switch (ItemVariant.valueOf(variant.toUpperCase(Locale.ROOT))){
                case STONE -> {setMaterial(Material.STONE_PICKAXE);}
                case WOOD -> {setMaterial(Material.WOODEN_PICKAXE);}
                case IRON -> {setMaterial(Material.IRON_PICKAXE);}
                case GOLD -> {setMaterial(Material.GOLDEN_PICKAXE);}
                case DIAMOND -> {setMaterial(Material.DIAMOND_PICKAXE);}
            }
        } catch (IllegalArgumentException e){
            setMaterial(Material.WOODEN_PICKAXE);
        }
    }
    
    @EventHandler
    public static void onPlayerDestroyBlock(BlockBreakEvent event){
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Block target = event.getBlock();
        if(target.isValidTool(item) && TiboiseItem.getItemId(item).equals(getId(Hammer.class)) && !event.getPlayer().isSneaking()){
            radiusBreak(RadiusType.SQUARE,1,event.getBlock(), Objects.requireNonNull(event.getPlayer().getTargetBlockFace(8)),event.getPlayer().getInventory().getItemInMainHand(),durabilitymultiplier,event.getPlayer());
        }
    }
    
    @Override
    public @Nullable ShapedRecipe getRecipe(ItemVariant variant) {
        if(!getPossibleVariants().contains(variant)){
            return null;
        }
        
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Tiboise.getPlugin(),variant.toString().toLowerCase(Locale.ROOT)+"_"+getId()),new Hammer(variant).get());
        recipe.shape(
                "MMM",
                "MMM",
                "XSX"
        );
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        
        Material mat = Material.AIR;
        RecipeChoice choices = null;
        switch(variant){
            case WOOD -> choices = new RecipeChoice.MaterialChoice(Tag.PLANKS);
            case STONE -> choices = new RecipeChoice.MaterialChoice(Tag.ITEMS_STONE_TOOL_MATERIALS);
            case IRON -> mat = Material.IRON_INGOT;
            case GOLD -> mat = Material.GOLD_INGOT;
            case DIAMOND -> mat = Material.DIAMOND;
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
                ItemVariant.DIAMOND
        );
    }
    
    @Override
    public String getRepairMaterialId() {
        return TiboiseItem.getItemId(new ItemStack(Material.IRON_INGOT));
    }
    
    @Override
    public int getRepairPointPerItem() {
        return 0;
    }
}
