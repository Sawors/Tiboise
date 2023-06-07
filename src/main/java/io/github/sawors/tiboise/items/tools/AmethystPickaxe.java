package io.github.sawors.tiboise.items.tools;

import io.github.sawors.tiboise.items.DurabilityItem;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class AmethystPickaxe extends TiboiseItem implements DurabilityItem, Listener {
    
    final static private Material pickaxeMaterial = Material.IRON_PICKAXE;
    
    public AmethystPickaxe(){
        setMaterial(pickaxeMaterial);
        setHelpText("This pickaxe behaves like an iron pickaxe enchanted with Silk Touch");
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get());
        recipe.shape(
                "AIA",
                "XSX",
                "XSX"
        )
                .setIngredient('A', Material.AMETHYST_SHARD)
                .setIngredient('I', Material.IRON_INGOT)
                .setIngredient('S',Material.STICK);
        return recipe;
        
    }
    
    @Override
    public String getRepairMaterialId() {
        return getItemId(new ItemStack(Material.AMETHYST_SHARD));
    }
    
    @Override
    public int getRepairPointPerItem() {
        return 63;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public static void doSilkTouchReplacement(BlockDropItemEvent event){
        Player p = event.getPlayer();
        BlockState b = event.getBlockState();
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if(getItemId(handItem).equals(getId(AmethystPickaxe.class))){
            logAdmin("YES");
            List<Item> drops = event.getItems();
            drops.removeIf(i -> b.getDrops(new ItemStack(pickaxeMaterial)).stream().map(ItemStack::getType).anyMatch(m -> m.equals(i.getItemStack().getType())));
            drops.clear();
            ItemStack fakeSilkTouch = new ItemStack(pickaxeMaterial);
            fakeSilkTouch.addEnchantment(Enchantment.SILK_TOUCH,1);
            Set<Item> newDrops = new HashSet<>();
            logAdmin(b.getDrops(fakeSilkTouch));
            logAdmin(fakeSilkTouch);
            logAdmin(b.getType());
            for(ItemStack i : b.getDrops(fakeSilkTouch)){
                newDrops.add(b.getWorld().dropItem(b.getLocation().add(.5,.5,.5),i));
            }
            drops.addAll(newDrops);
            
            
            
        }
    }
    
    @EventHandler
    public static void doSilkTouchReplacement2(BlockBreakEvent event){
        Player p = event.getPlayer();
        Block b = event.getBlock();
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if(getItemId(handItem).equals(getId(AmethystPickaxe.class))){
            event.setCancelled(true);
            final ItemStack fakeSilkTouch = new ItemStack(Material.IRON_PICKAXE);
            fakeSilkTouch.addEnchantment(Enchantment.SILK_TOUCH,1);
            b.breakNaturally(fakeSilkTouch,false);
        }
    }
}
