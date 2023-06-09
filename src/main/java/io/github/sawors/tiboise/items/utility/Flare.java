package io.github.sawors.tiboise.items.utility;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class Flare extends TiboiseItem implements Listener {
    
    private static final Map<UUID,Integer> launchedFlares = new HashMap<>();
    private final static int radius = 16;
    private final static int glowDuration = 8;
    
    public Flare(){
        setMaterial(Material.FIREWORK_ROCKET);
        setLore(List.of(
                Component.text("Useful when lost or when looking for someone").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
        setDisplayName(Component.text("Red Flare").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setHelpText("Launch it from ground or shoot it with a crossbow to reveal your position and nearby entities. Only entities having no opaque block above their head will be revealed. Flares shot from a crossbow have a wider radius");
    }
    
    @Override
    public ItemStack get(){
        ItemStack baseOutput = super.get();
        if(baseOutput.getItemMeta() instanceof FireworkMeta meta){
            meta.addEffect(FireworkEffect.builder().trail(true).flicker(true).withColor(Color.fromRGB(0x800000)).with(FireworkEffect.Type.BALL_LARGE).build());
            meta.setPower(2);
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
            baseOutput.setItemMeta(meta);
        }
        
        return baseOutput;
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(this.getIdAsKey(), this.get());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.RED_DYE);
        recipe.addIngredient(Material.GUNPOWDER);
        recipe.addIngredient(Material.GLOWSTONE_DUST);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
    
    @EventHandler
    public void trackFlaresCrossbow(EntityShootBowEvent event){
        logAdmin("trigger");
        if(event.getProjectile() instanceof Firework f && event.getBow() != null && event.getBow().getType().equals(Material.CROSSBOW) && TiboiseItem.getItemId(event.getConsumable()).equals(getId(Flare.class))){
            logAdmin("YES!!");
            launchedFlares.put(f.getUniqueId(),2);
        }
    }
    
    @EventHandler
    public void trackFlaresGround(PlayerLaunchProjectileEvent event){
        if(event.getProjectile() instanceof Firework f && TiboiseItem.getItemId(event.getItemStack()).equals(getId(Flare.class))){
            launchedFlares.put(f.getUniqueId(),1);
        }
    }
    
    @EventHandler
    public static void flareEffect(FireworkExplodeEvent event){
        Firework firework = event.getEntity();
        final UUID fireworkId = firework.getUniqueId();
        if(launchedFlares.containsKey(fireworkId)){
            final int period = 20;
            final int power = launchedFlares.get(fireworkId);
            final Location baseLocation = firework.getLocation();
            final Vector displacement = new Vector(.5,-1.25,0).rotateAroundY(Math.random()*6.28);
            Collection<LivingEntity> nearby = baseLocation.getNearbyLivingEntities(radius*((power+1)/2.0),48, e -> {
                final Location eLoc = e.getLocation();
                final Block highest = e.getWorld().getHighestBlockAt(eLoc);
                return (((eLoc.getY() <= highest.getY()) && (!highest.getType().isOccluding() || highest.getType().equals(Material.SNOW))) || (eLoc.getY() > highest.getY())) && eLoc.getY() >= e.getWorld().getSeaLevel();
            });
            for(LivingEntity entity : nearby){
                entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,glowDuration*20,1,true,false,false));
            }
            launchedFlares.remove(fireworkId);
            new BukkitRunnable(){
                int counter = 0;
                Location lastLoc = baseLocation;
                @Override
                public void run() {
                    counter ++;
                    lastLoc.add(displacement);
                    Firework additional = (Firework) lastLoc.getWorld().spawnEntity(lastLoc, EntityType.FIREWORK);
                    FireworkMeta smallerMeta = firework.getFireworkMeta().clone();
                    smallerMeta.clearEffects();
                    smallerMeta.addEffect(FireworkEffect.builder().withColor(firework.getFireworkMeta().getEffects().get(0).getColors()).with(FireworkEffect.Type.BALL).withTrail().build());
                    additional.setFireworkMeta(smallerMeta);
                    additional.detonate();
//                    baseLocation.getWorld().spawnParticle(Particle.REDSTONE,displaced,64*power, power, power, power,.1, new Particle.DustOptions(firework.getFireworkMeta().getEffects().get(0).getColors().get(0),2));
//                    baseLocation.getWorld().spawnParticle(Particle.REDSTONE,displaced,16*power,.33*power,.33*power,.33*power,.1, new Particle.DustOptions(Color.WHITE,1));
                    if(counter >= (glowDuration-1)*(20.0/period)){
                        this.cancel();
                    }
                }
            }.runTaskTimer(Tiboise.getPlugin(),period,period);
        }
    }
}
