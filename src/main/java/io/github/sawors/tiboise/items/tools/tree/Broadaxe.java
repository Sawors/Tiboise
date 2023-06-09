package io.github.sawors.tiboise.items.tools.tree;

import com.destroystokyo.paper.MaterialSetTag;
import com.google.common.collect.Lists;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.ItemVariant;
import io.github.sawors.tiboise.items.TiboiseItem;
import io.github.sawors.tiboise.items.tools.VeinMinerUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Broadaxe extends TiboiseItem implements Listener {
    
    private static Set<Material> filter = new HashSet<>() {{
        addAll(MaterialSetTag.LOGS.getValues());
        add(Material.MANGROVE_ROOTS);
    }};
    
    private static final boolean animate = false;
    private static final boolean reverseanimation = true;
    private static final int period = 1;
    private static final boolean doitemdamage = true;
    private static final double damagemodifier = .85;
    
    /**
     * NEVER USE THIS ALONE IF IT IS TO CREATE AN ITEM
     */
    public Broadaxe(){
    }
    
    public Broadaxe(ItemVariant variant){
        setVariant(variant.toString());
    }
    
    
    @EventHandler
    public static void onPlayerBreakTree(BlockBreakEvent event){
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if(filter.contains(event.getBlock().getType()) && TiboiseItem.getItemId(tool).equals(getId(Broadaxe.class))){
            int reach = 0;
            switch(TiboiseItem.getItemVariant(tool)){
                case "wood" -> reach = 4;
                case "stone", "gold" -> reach = 8;
                case "iron" -> reach = 16;
                case "diamond", "steel", "netherite" -> reach = 32;
            }
            cutTreeFromBlock(event.getBlock(),CuttingMode.VERTICAL,tool, reach, event.getPlayer());
        }
    }
    private static void cutTreeFromBlock(Block origin, CuttingMode mode, ItemStack tool, int limit){
        cutTreeFromBlock( origin, mode, tool, limit, null);
    }
    private static void cutTreeFromBlock(Block origin, CuttingMode mode, ItemStack tool, int limit, @Nullable Player cutter){
        List<Block> cutlist = new ArrayList<>();
        switch(mode) {
            case EXTENDED -> cutlist = new VeinMinerUtility(origin).setLimit(limit).getVein(filter);
            case VERTICAL -> cutlist = new VeinMinerUtility(origin).setLimit(limit).getVerticalVein(false,filter);
            case VERTICAL_DOUBLE -> cutlist = new VeinMinerUtility(origin).setLimit(limit).getVerticalVein(true,filter);
        }
        boolean animlocal = !(mode == CuttingMode.VERTICAL_DOUBLE || mode == CuttingMode.VERTICAL) && animate;
        if(doitemdamage && tool.getItemMeta() instanceof Damageable d && !d.isUnbreakable() && cutter != null){
            tool.damage((int)(cutlist.size()*damagemodifier),cutter);
        }
        if(animlocal){
            final List<Block> f_cutlist = cutlist;
            new BukkitRunnable(){
                int it = 0;
                final List<Block> reversed = reverseanimation ? Lists.reverse(f_cutlist) : f_cutlist;
                final int limit = reversed.size();
                @Override
                public void run(){
                    if(it >= limit){
                        this.cancel();
                        return;
                    }
                    Block condamned = reversed.get(it);
                    condamned.breakNaturally(tool,true);
            
                    it++;
                }
            }.runTaskTimer(Tiboise.getPlugin(),0, period);
        } else {
            for(Block b : cutlist){
                b.breakNaturally(tool,true);
            }
        }
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
    public @Nullable ShapedRecipe getRecipe(ItemVariant variant) {
        if(!getPossibleVariants().contains(variant)){
            return null;
        }
        
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Tiboise.getPlugin(),variant.toString().toLowerCase(Locale.ROOT)+"_"+getId()),new Broadaxe(variant).get());
        recipe.shape(
                "MMM",
                "MSX",
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
    public void setVariant(String variant) {
        super.setVariant(variant);
        String varname = variant.toLowerCase(Locale.ROOT);
        String append = variant.equalsIgnoreCase("gold") || variant.equalsIgnoreCase("wood") ? "en" : "";
        setDisplayName(Component.text(Tiboise.getUpperCamelCase(Character.toUpperCase(varname.charAt(0))+varname.substring(1)+append+" "+getId())).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        try{
            switch (ItemVariant.valueOf(variant.toUpperCase(Locale.ROOT))){
                case STONE -> {setMaterial(Material.STONE_AXE);}
                case WOOD -> {setMaterial(Material.WOODEN_AXE);}
                case IRON -> {setMaterial(Material.IRON_AXE);}
                case GOLD -> {setMaterial(Material.GOLDEN_AXE);}
                case DIAMOND -> {setMaterial(Material.DIAMOND_AXE);}
            }
        } catch (IllegalArgumentException e){
            setMaterial(Material.WOODEN_AXE);
        }
    }
}
