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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DivingLeggings extends TiboiseItem implements Listener, DurabilityItem {
    public DivingLeggings(){
        setMaterial(Material.CHAINMAIL_LEGGINGS);
        setDisplayName(Component.text("Diving Leggings").color(NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setShortLore("Makes mining underwater faster");
        addTag(ItemTag.PREVENT_BREAKING);
    }
    
    // COPY ARMOR EFFECT START
    private static final Map<UUID, BukkitRunnable> playerCheck = new HashMap<>();
    @EventHandler
    public static void scubaLeggingsEffect(PlayerArmorChangeEvent event){
        // EDIT
        final String refId = getId(DivingLeggings.class);
        
        final Player p = event.getPlayer();
        final UUID pid = p.getUniqueId();
        if(!playerCheck.containsKey(pid) && getItemId(event.getNewItem()).equals(refId)){
            
            // how often should the check be done (in seconds)
            // EDIT
            final int period = 4;
            BukkitRunnable check = new BukkitRunnable(){
                @Override
                public void run() {
                    // EDIT
                    final EquipmentSlot slot = EquipmentSlot.LEGS;
                    
                    final ItemStack item = event.getPlayer().getInventory().getItem(slot);
                    if(getItemId(item).equals(refId) && item.getItemMeta() instanceof Damageable dmg && dmg.getDamage() < item.getType().getMaxDurability()-1){
                        if(p.isUnderWater()){
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,20*(period+1),3,true,false,false));
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
    // COPY ARMOR EFFECT END
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get());
        recipe.shape(
                "PCP",
                "CXC",
                "CXC"
        )
            .setIngredient('C',Material.COPPER_INGOT)
            .setIngredient('P',Material.PRISMARINE_SHARD);
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
