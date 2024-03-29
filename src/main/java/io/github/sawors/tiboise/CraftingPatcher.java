package io.github.sawors.tiboise;

import io.github.sawors.tiboise.items.DurabilityItem;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;

import java.util.Arrays;
import java.util.List;

public class CraftingPatcher implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public static void preventVanillaExploit(PrepareItemCraftEvent event){
        Recipe r = event.getRecipe();
        if(event.getInventory().getResult() == null || r == null) return;
        if(Arrays.stream(event.getInventory().getMatrix()).anyMatch(TiboiseItem::isTiboiseItem)){
            if(Arrays.stream(event.getInventory().getMatrix()).allMatch(c -> (!TiboiseItem.isTiboiseItem(c) || !TiboiseItem.getItemTags(c).contains(ItemTag.PREVENT_USE_IN_CRAFTING.toString())))) return;
            final ItemStack result = event.getInventory().getResult();
            final TiboiseItem tiboiseResult = TiboiseItem.getRegisteredItem(TiboiseItem.getItemId(result));
            boolean cancelEvent = false;
            if(tiboiseResult != null){
                for(ItemStack item : event.getInventory().getMatrix()){
                    if(TiboiseItem.getItemTags(item).contains(ItemTag.INGREDIENT.toString())){
                        // item is a Tiboise ingredient
                        if(r instanceof ShapedRecipe recipe && tiboiseResult.getRecipe() instanceof ShapedRecipe refRecipe){
                            cancelEvent = !recipe.getChoiceMap().equals(refRecipe.getChoiceMap());
                            if(cancelEvent) break;
                        } else if(r instanceof ShapelessRecipe recipe && tiboiseResult.getRecipe() instanceof ShapelessRecipe refRecipe){
                            // may have poor performance
                            cancelEvent = !recipe.getChoiceList().containsAll(refRecipe.getChoiceList());
                            if(cancelEvent) break;
                        } else if(r instanceof CookingRecipe<?> recipe && tiboiseResult.getRecipe() instanceof CookingRecipe<?> refRecipe){
                            cancelEvent = !recipe.getInputChoice().equals(refRecipe.getInputChoice());
                            if(cancelEvent) break;
                        } else if(r instanceof SmithingRecipe recipe && tiboiseResult.getRecipe() instanceof SmithingRecipe refRecipe){
                            cancelEvent = !(List.of(recipe.getBase(),recipe.getAddition()).containsAll(List.of(refRecipe.getBase(),refRecipe.getAddition())));
                            if(cancelEvent) break;
                        } else if(r instanceof StonecuttingRecipe recipe && tiboiseResult.getRecipe() instanceof StonecuttingRecipe refRecipe){
                            cancelEvent = !recipe.getInputChoice().equals(refRecipe.getInputChoice());
                            if(cancelEvent) break;
                        }
                    } else if(TiboiseItem.isTiboiseItem(item) && !TiboiseItem.getItemTags(item).contains(ItemTag.INGREDIENT.toString())){
                        cancelEvent = true;
                        break;
                    }
                }
                
            }
            if(cancelEvent){
                event.getInventory().setResult(null);
            }
            // Troubles begin now
            //if(r instanceof MerchantRecipe recipe){
            //            event.setCancelled(!recipe.getIngredients().stream().map(TiboiseItem::getItemId).sorted().equals(matrixIds));
            //        }else if(r instanceof ShapedRecipe recipe){
            //            recipe.getChoiceMap().values().
            //        } else if(r instanceof ShapelessRecipe recipe){
            //
            //        } else if(r instanceof SmithingRecipe recipe){
            //
            //        } else if(r instanceof StonecuttingRecipe recipe){
            //
            //        }
        }
        
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public static void handleAnvilRepair(PrepareAnvilEvent event){
        if(event.getInventory().getFirstItem() == null) return;
        final AnvilInventory inventory = event.getInventory();
        final ItemStack second = inventory.getSecondItem();
        final ItemStack first = inventory.getFirstItem();
        if(!TiboiseItem.isTiboiseItem(first) && !TiboiseItem.isTiboiseItem(second)) return;
        
        final TiboiseItem item = TiboiseItem.getRegisteredItem(TiboiseItem.getItemId(inventory.getFirstItem()));
        
        if(inventory.getFirstItem() != null && item instanceof DurabilityItem dura){
            if(second == null && event.getInventory().getRenameText() != null && event.getInventory().getRenameText().length() >= 1) return;
            if(second != null && dura.getRepairMaterialId().equals(TiboiseItem.getItemId(second))) {
                LivingEntity e = event.getViewers().get(0);
                if(e != null){
                    ItemStack repairable = inventory.getFirstItem().clone();
                    
                    if(repairable.getItemMeta() instanceof Damageable dmg){
                        if(dmg.getDamage() == 0){
                            event.setResult(null);
                            return;
                        }
                        final int available = second.getAmount();
                        final int baseDamage = dmg.getDamage();
                        final int repairPoints = dura.getRepairPointPerItem();
                        int amountConsumed = 1;
                        while(amountConsumed < available && repairPoints*amountConsumed < baseDamage){
                            amountConsumed++;
                        }
                        event.getInventory().setRepairCostAmount(amountConsumed);
                        dmg.setDamage(Math.max(dmg.getDamage()-(repairPoints*available),0));
                        repairable.setItemMeta(dmg);
                        event.setResult(repairable);
                        for(HumanEntity viewer : event.getViewers()){
                            viewer.playSound(Sound.sound(org.bukkit.Sound.BLOCK_ANVIL_USE, Sound.Source.PLAYER,1,1));
                        }
                    }
                    
                }
            } else {
                event.setResult(null);
            }
        } else if(TiboiseItem.isTiboiseItem(second)){
            //
            // IMPORTANT : For the moment Tiboise items can't be used for reparations
            //
            event.setResult(null);
        }
    }
}
