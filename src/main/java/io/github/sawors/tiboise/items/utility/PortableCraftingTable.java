package io.github.sawors.tiboise.items.utility;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortableCraftingTable extends TiboiseItem implements Listener {
    
    public PortableCraftingTable(){
        super();
        
        setMaterial(Material.LEATHER);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setLore(List.of(
                Component.text("This item allows you to open").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("a crafting table on the go").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
    }
    
    
    @EventHandler
    public static void openCraftingInventory(PlayerInteractEvent event){
        if(event.getItem() != null && TiboiseItem.getItemId(event.getItem()).equals(getId(PortableCraftingTable.class))){
            event.getPlayer().openWorkbench(event.getPlayer().getLocation(),true);
            event.getPlayer().playSound(event.getPlayer().getLocation(),Sound.ENTITY_VILLAGER_WORK_MASON,.75f,1.25f);
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Tiboise.getPlugin(),getId()),new PortableCraftingTable().get());
        recipe.shape(
                "XLX",
                "LCL",
                "XLX"
        );
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('X', Material.AIR);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
