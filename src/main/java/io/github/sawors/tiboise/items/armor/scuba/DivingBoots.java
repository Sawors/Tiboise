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
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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

public class DivingBoots extends TiboiseItem implements Listener, DurabilityItem {
    public DivingBoots(){
        setMaterial(Material.CHAINMAIL_BOOTS);
        setDisplayName(Component.text("Diving Boots").color(NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setShortLore("Make you swim faster");
        addTag(ItemTag.PREVENT_BREAKING);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
    }
    
    // COPY ARMOR EFFECT START
    private static final Map<UUID, BukkitRunnable> playerCheck = new HashMap<>();
    private static final String modifierName = "scuba swim speed bonus";
    private final static int period = 3;
    @EventHandler
    public static void scubaBootsEffect(PlayerArmorChangeEvent event){
        // EDIT
        final String refId = getId(DivingBoots.class);
        
        final Player p = event.getPlayer();
        final UUID pid = p.getUniqueId();
        if(!playerCheck.containsKey(pid) && getItemId(event.getNewItem()).equals(refId)){
            
            // how often should the check be done (in seconds)
            // -> shorter period for the boots since the speed bonus is really OP when outside of water
            
            
            
            BukkitRunnable check = new BukkitRunnable(){
                @Override
                public void run() {
                    
                    if(!p.isOnline()){
                        this.cancel();
                        playerCheck.remove(p.getUniqueId());
                        return;
                    }
                    
                    // EDIT
                    final EquipmentSlot slot = EquipmentSlot.FEET;
                    
                    final ItemStack item = event.getPlayer().getInventory().getItem(slot);
                    final org.bukkit.attribute.AttributeInstance attributeInstance = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
                    if(getItemId(item).equals(refId) && item.getItemMeta() instanceof Damageable dmg && dmg.getDamage() < item.getType().getMaxDurability()-1){
                        // HERE FOR THE EFFECTS
                        
                        if(p.isInWater()){
                            item.damage(1,p);
                            final AttributeModifier modifier = new AttributeModifier(modifierName,.03, AttributeModifier.Operation.ADD_NUMBER);
                            if(attributeInstance != null && !attributeInstance.getModifiers().contains(modifier)){
                                attributeInstance.addModifier(modifier);
                            }
                        } else {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20*(period+1),1,true,false,true));
                        }
                    } else {
                        if(attributeInstance != null){
                            attributeInstance.getModifiers().forEach(m -> {
                                if(m.getName().equals(modifierName)){
                                    attributeInstance.removeModifier(m);
                                }
                            });
                        }
                        this.cancel();
                        playerCheck.remove(pid);
                    }
                }
            };
            check.runTaskTimer(Tiboise.getPlugin(),0,period*20);
            playerCheck.put(pid,check);
            
            new BukkitRunnable(){
                @Override
                public void run() {
                    final Player p = event.getPlayer();
                    if(!p.isOnline() || !playerCheck.containsKey(p.getUniqueId())) {
                        this.cancel();
                        return;
                    }
                    final Block ref = p.getLocation().getBlock();
                    if(!(ref.getType() == Material.WATER || (ref.getBlockData() instanceof Waterlogged w && w.isWaterlogged()))){
                        // exits water
                        final org.bukkit.attribute.AttributeInstance attributeInstance = event.getPlayer().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
                        if(attributeInstance != null){
                            attributeInstance.getModifiers().forEach(m -> {
                                if(m.getName().equals(modifierName)){
                                    attributeInstance.removeModifier(m);
                                }
                            });
                        }
                    }
                }
            }.runTaskTimer(Tiboise.getPlugin(),1,10);
        }
    }
    // COPY ARMOR EFFECT END
    
    @EventHandler
    public static void removeSpeedOnConnection(PlayerJoinEvent event){
        final org.bukkit.attribute.AttributeInstance attributeInstance = event.getPlayer().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
        if(attributeInstance != null && attributeInstance.getModifiers().stream().anyMatch(m -> m.getName().equals(modifierName))){
            attributeInstance.getModifiers().forEach(m -> {
                if(m.getName().equals(modifierName)){
                    attributeInstance.removeModifier(m);
                }
            });
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemKey(),this.get());
        recipe.shape(
                "PXP",
                "CXC"
        );
        recipe.setIngredient('C',Material.COPPER_INGOT);
        recipe.setIngredient('P',Material.PRISMARINE_SHARD);
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
