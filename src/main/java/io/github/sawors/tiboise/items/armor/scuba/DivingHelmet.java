package io.github.sawors.tiboise.items.armor.scuba;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.DurabilityItem;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DivingHelmet extends TiboiseItem implements Listener, DurabilityItem {
    
    public DivingHelmet(){
        setMaterial(Material.CHAINMAIL_HELMET);
        setDisplayName(Component.text("Diving Helmet").color(NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setShortLore("Helps you breath underwater");
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        addTag(ItemTag.PREVENT_BREAKING);
    }
    
    private static Map<UUID, BukkitRunnable> playerCheck = new HashMap<>();
    
    @EventHandler
    public static void scubaHelmetEffect(PlayerArmorChangeEvent event){
        final String refId = getId(DivingHelmet.class);
        final Player p = event.getPlayer();
        final UUID pid = p.getUniqueId();
        if(!playerCheck.containsKey(pid) && getItemId(event.getNewItem()).equals(refId)){
            
            // how often should the check be done (in seconds)
            final int period = 4;
            BukkitRunnable check = new BukkitRunnable(){
                @Override
                public void run() {
                    final EquipmentSlot slot = EquipmentSlot.HEAD;
                    final ItemStack item = event.getPlayer().getInventory().getItem(slot);
                    if(getItemId(item).equals(refId) && item.getItemMeta() instanceof Damageable dmg && dmg.getDamage() < item.getType().getMaxDurability()-1){
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,20*(period+1),0,true,false,false));
                        if(p.isUnderWater()){
                            p.damageItemStack(slot,1);
                        }
                    } else {
                        this.cancel();
                        playerCheck.remove(pid);
                    }
                }
            };
            check.runTaskTimer(Tiboise.getPlugin(),0,period*20);
            playerCheck.put(pid,check);
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get());
        recipe.shape(
                "CCC",
                "GHG",
                "PSP"
        );
        recipe.setIngredient('C',Material.COPPER_INGOT);
        recipe.setIngredient('P',Material.PRISMARINE_SHARD);
        recipe.setIngredient('G',Material.TINTED_GLASS);
        recipe.setIngredient('H',Material.HEART_OF_THE_SEA);
        recipe.setIngredient('S',Material.PRISMARINE_CRYSTALS);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
    
    @Override
    public String getRepairMaterialId() {
        return getItemId(new ItemStack(Material.COPPER_INGOT));
    }
    
    @Override
    public int getRepairPointPerItem() {
        return 35;
    }
}
