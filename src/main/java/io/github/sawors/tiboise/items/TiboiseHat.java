package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.Tiboise;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public abstract class TiboiseHat extends TiboiseItem implements Listener {
    
    protected void setDefaultHatData(){
        addTag(ItemTag.HAT);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setHelpText("Right click while looking up with this hat in your hand to wear it. To take it off with style, look down and right click while sneaking with an empty hand.");
    }
    
    @EventHandler
    public void hatWearAction(PlayerInteractEvent event){
        final Player p = event.getPlayer();
        final PlayerInventory inv = p.getInventory();
        final ItemStack item = inv.getItemInMainHand();
        final ItemStack hat = inv.getHelmet();
        final float pitch = p.getLocation().getPitch();
        final Set<String> itemTags = getItemTags(item);
        
        if(
                event.getAction().isRightClick()
                && event.getHand() != null
                && event.getHand().equals(EquipmentSlot.HAND)
                && (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof TileState))
        ) {
            if(pitch <= -45 && itemTags.contains(ItemTag.HAT.toString())){
                // check for already equipped hat
                if(hat != null){
                    Set<String> hatTags = getItemTags(hat);
                    if(hatTags.contains(ItemTag.HAT.toString()) && !hatTags.contains(ItemTag.UNMOVABLE.toString())){
                        // take off previous hat
                        final ItemStack tempHat = hat.clone();
                        inv.setHelmet(item);
                        item.setAmount(item.getAmount()-1);
                        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5f);
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            }
                        }.runTaskLater(Tiboise.getPlugin(),20);
                        // giving back the removed hat
                        if(inv.getItemInMainHand().getType().isAir()){
                            inv.setItemInMainHand(tempHat);
                        } else {
                            for(ItemStack overflow : inv.addItem(tempHat).values()){
                                p.getWorld().dropItem(p.getLocation(),overflow);
                            }
                        }
                    } else {
                        // prevent helmet switching
                        p.sendActionBar(Component.text("You already have a helmet, only hats can be switched"));
                    }
                } else {
                    // wear hat (default way)
                    inv.setHelmet(item);
                    item.setAmount(item.getAmount()-1);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                }
               
                
            } else if(pitch >= 45 && item.getType().isAir() && hat != null && getItemTags(hat).contains(ItemTag.HAT.toString())){
                // taking off hat
                inv.setItemInMainHand(hat);
                inv.setHelmet(null);
                p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5F);
            }
        }
    }
    
    // make sure the returned item is a hat so implementations don't have to specify it
    @Override
    public ItemStack get() {
        addTag(ItemTag.HAT);
        return super.get();
    }
    
    /*@EventHandler
    public static void hatWearAction(PlayerInteractEvent event){
        Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        Block b = event.getClickedBlock();
        String id = StonesItem.getItemId(item);
        List<String> taglist = StonesItem.getItemTags(item);
        
        if (item.hasItemMeta() && taglist.size() > 0) {
            //WEAR HAT
            if (p.getLocation().getPitch() <= -85 && b == null) {
                if (taglist.contains(ItemTag.HAT.tagString())) {
                    p.getInventory().setItemInMainHand(p.getInventory().getHelmet());
                    if(getItemTags(p.getInventory().getHelmet()).contains(ItemTag.HAT.tagString())){
                        // calling wear hat event when removing the previous hat is there is one
                        PlayerWearHatEvent hatevent = new PlayerWearHatEvent(p, item, false);
                        Bukkit.getPluginManager().callEvent(hatevent);
                    }
                    p.getInventory().setHelmet(item);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    
                    // calling wear hat event
                    Stones.logAdmin("wearing");
                    PlayerWearHatEvent hatevent = new PlayerWearHatEvent(p, item, true);
                    Bukkit.getPluginManager().callEvent(hatevent);
                }
            }
        } else if (item.getType() == Material.AIR) {
            //"unwear" hat
            ItemStack helmet = p.getInventory().getHelmet();
            if (p.getInventory().getItemInMainHand().getType() == Material.AIR && helmet != null && getItemTags(helmet).contains(ItemTag.HAT.tagString()) && p.isSneaking() && p.getLocation().getPitch() >= 85) {
                if (!taglist.contains(ItemTag.UNMOVABLE.tagString())) {
                    p.getInventory().setItemInMainHand(helmet);
                    p.getInventory().setHelmet(null);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.5F);
                    // calling wear hat event
                    PlayerWearHatEvent hatevent = new PlayerWearHatEvent(p, item, false);
                    Bukkit.getPluginManager().callEvent(hatevent);
                }
                
            }
        }
    }*/
}
