package io.github.sawors.tiboise.core;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.type.Chain;
import org.bukkit.block.data.type.Door;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QOLImprovements implements Listener {
    
    
    //
    //  DISABLE RESPAWN ANCHORS
    @EventHandler(priority = EventPriority.LOW)
    public void disableRespawnAnchors(PlayerInteractEvent event){
        if(event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.RESPAWN_ANCHOR) && event.getAction().isRightClick() && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GLOWSTONE)){
            event.setCancelled(true);
        }
    }
    //
    //  CHAIN CLIMB
    @EventHandler
    public void chainClimber(PlayerInteractEvent event){
        Player p = event.getPlayer();
        
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHAIN && p.getLocation().add(0,1,0).getBlock().getType().equals(Material.CHAIN) && ((Chain) p.getLocation().add(0,1,0).getBlock().getBlockData()).getAxis().equals(Axis.Y)){
            if(p.isSneaking()){
                p.setVelocity(new Vector(0,.25,0));
                p.getWorld().playSound(p.getLocation().add(0,1,0), Sound.BLOCK_CHAIN_STEP, .25f, 1);
            }else{
                p.setVelocity(new Vector(0,.33,0));
                p.getWorld().playSound(p.getLocation().add(0,1,0), Sound.BLOCK_CHAIN_STEP, 1, 1);
            }
        }
    }
    //
    //  OPEN DOUBLE DOORS
    @EventHandler
    public void onPlayerOpenDoor(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        if(b != null && b.getBlockData() instanceof Door door && event.getAction().isRightClick()){
            if(door.getMaterial().equals(Material.IRON_DOOR)){
                return;
            }
            
            
            
            //DOUBLE DOOR LOGIC
            if(!event.useInteractedBlock().equals(Event.Result.DENY) && !event.getPlayer().isSneaking()) {
                Block b1;
                Block b2;
                if (door.getFacing().equals(BlockFace.NORTH) || door.getFacing().equals(BlockFace.SOUTH)) {
                    b1 = b.getLocation().add(1, 0, 0).getBlock();
                    b2 = b.getLocation().add(-1, 0, 0).getBlock();
                    
                    
                } else {
                    b1 = b.getLocation().add(0, 0, 1).getBlock();
                    b2 = b.getLocation().add(0, 0, -1).getBlock();
                }
                if (b1.getBlockData() instanceof Door d1 && d1.getHinge() != door.getHinge()) {
                    if (door.isOpen()) {
                        d1.setOpen(false);
                        b1.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
                    } else {
                        d1.setOpen(true);
                        b1.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_OPEN, 1, 1);
                    }
                    b1.setBlockData(d1);
                    b1.getState().update();
                    
                }
                if (b2.getBlockData() instanceof Door d2 && d2.getHinge() != door.getHinge()) {
                    if (door.isOpen()) {
                        d2.setOpen(false);
                        b2.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
                    } else {
                        d2.setOpen(true);
                        b2.getWorld().playSound(b1.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_WOODEN_DOOR_OPEN, 1, 1);
                    }
                    b2.setBlockData(d2);
                    b2.getState().update();
                }
            }
        }
        if(b != null && b.getType().toString().contains("_DOOR") && event.getAction().isLeftClick() && event.getPlayer().getInventory().getItemInMainHand().getType().isAir()){
            if(event.getPlayer().isSneaking()){
                b.getWorld().playSound(b.getLocation().add(.5,0,.5), "minecraft:sawors.door.knock", .25f, randomPitchSimple()-0.5f);
            }else{
                b.getWorld().playSound(b.getLocation().add(.5,0,.5), "minecraft:sawors.door.knock", 1, randomPitchSimple());
            }
        }
    }
    //
    //  TORCH BURN BLOCKS WHEN DROPPED
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event){
        if(event.getEntity().getItemStack().getType() == Material.TORCH && Math.random() < 0.5){
            event.getEntity().getLocation().getBlock().setType(Material.FIRE);
        }
    }
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event){
        if(event.getEntity().getItemStack().getType() == Material.TORCH){
            event.getEntity().setTicksLived(4800);
        }
    }
    //
    //  ARROW SOUND + BREAK BLOCKS
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event){
        if(event.getHitBlock() != null && event.getEntity() instanceof Arrow arrow){
            Block block = event.getHitBlock();
            if(block.getType().getBlastResistance() > 1){
                if(Math.random() < block.getType().getBlastResistance()/10){
                    arrow.getWorld().spawnParticle(Particle.ITEM_CRACK, arrow.getLocation(),2,.1,.1,.1,.1,new ItemStack(Material.STICK));
                    arrow.getWorld().spawnParticle(Particle.ITEM_CRACK, arrow.getLocation(),4,.1,.1,.1,.1,new ItemStack(Material.IRON_INGOT));
                    block.getWorld().playSound(block.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, randomPitchSimple()+2f);
                    arrow.remove();
                } else {
                    block.getWorld().playSound(block.getLocation(), block.getBlockSoundGroup().getPlaceSound(), 1f, randomPitchSimple()+0.5f);
                }
            } else {
                arrow.getWorld().spawnParticle(Particle.BLOCK_CRACK, arrow.getLocation(),6,.1,.1,.1,.1,block.getBlockData());
                block.getWorld().playSound(block.getLocation(), block.getBlockSoundGroup().getPlaceSound(), 1f, randomPitchSimple()+0.2f);
            }
            
            // switch for sound
            
            
            
            //arrow break reaction
            if(block.getType().toString().contains("GLASS_PANE")){
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1,1.2f);
                        block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getX()+0.5, block.getY()+0.5, block.getZ()+0.5,16, 0, 0,0, block.getBlockData());
                        block.getWorld().spawnParticle(Particle.FLAME, block.getLocation(), 1);
                        block.breakNaturally();
                    }
                }.runTaskLater(Main.getPlugin(), 1);
            }
        }
    }
    
    //
    // TORCH PUT THE TARGET ON FIRE
    @EventHandler
    public static void setEntityOnFireWhenHitWithTorch(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player p && Objects.equals(p.getInventory().getItemInMainHand().getType(),Material.TORCH)){
            event.getEntity().setFireTicks(20*2);
        }
    }
    
    //
    //  REMOVE XP
    @EventHandler
    public void onXpSpawn(EntityAddToWorldEvent event){
        if(event.getEntity() instanceof ExperienceOrb){
            new BukkitRunnable(){
                @Override
                public void run() {
                    event.getEntity().remove();
                }
            }.runTask(Main.getPlugin());
        }
    }
    
    //
    //  REMOVE XP EARNING
    @EventHandler
    public static void removeXpEarning(PlayerExpChangeEvent event){
        event.setAmount(0);
        event.getPlayer().setLevel(0);
    }
    @EventHandler
    public static void removeAdvancementXp(PlayerAdvancementDoneEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                event.getPlayer().setLevel(0);
                event.getPlayer().setExp(0);
            }
        }.runTaskLater(Main.getPlugin(),1);
    }
    
    //
    // SUBTLE LEAVE MESSAGES
    @EventHandler (priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event){
        if(event.quitMessage() != null){
            event.quitMessage(Component.text("← ").append(event.getPlayer().displayName()).color(TextColor.color(Color.GRAY.asRGB())));
            //
            //
        }
    }
    
    //
    // SUBTLE JOIN MESSAGES
    @EventHandler (priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event){
        if(event.joinMessage() != null){
            event.joinMessage(Component.text("→ ").append(event.getPlayer().displayName()).color(TextColor.color(Color.GRAY.asRGB())));
        }
    }
    
    //
    // MORE VANILLA RECIPE
    //
    @EventHandler
    public static void onLoad(PluginEnableEvent event){
        if(event.getPlugin().equals(Main.getPlugin())){
            List<Recipe> vanillabonusrecipes = new ArrayList<>();
            
            // SADDLES
            vanillabonusrecipes.add(
                    new ShapedRecipe(new NamespacedKey(Key.MINECRAFT_NAMESPACE,new ItemStack(Material.SADDLE).getType().toString().toLowerCase(Locale.ROOT)), new ItemStack(Material.SADDLE)).shape(
                            "LLL",
                            "SXS"
                    )
                    .setIngredient('X', Material.AIR)
                    .setIngredient('L', Material.LEATHER)
                    .setIngredient('S', Material.STRING)
            );
            // NAME TAGS
            vanillabonusrecipes.add(
                    new ShapelessRecipe(new NamespacedKey(Key.MINECRAFT_NAMESPACE,new ItemStack(Material.NAME_TAG).getType().toString().toLowerCase(Locale.ROOT)), new ItemStack(Material.NAME_TAG))
                    .addIngredient(Material.GOLD_NUGGET)
                    .addIngredient(Material.PAPER)
            );
            // MAKE GOLD ITEM ENCHANTED BY DEFAULT
            List<Material> goldtoedit = List.of(Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_AXE, Material.GOLDEN_SWORD);
            for (Material mat : goldtoedit){
                Map<Enchantment, Integer> enchantmap = new HashMap<>();
                switch (mat){
                    case GOLDEN_HOE, GOLDEN_SHOVEL -> enchantmap.put(Enchantment.SILK_TOUCH,1);
                    case GOLDEN_SWORD, GOLDEN_AXE -> enchantmap.put(Enchantment.LOOT_BONUS_MOBS,2);
                    case GOLDEN_PICKAXE -> enchantmap.put(Enchantment.LOOT_BONUS_BLOCKS,2);
                }
                for(Recipe r : Bukkit.getRecipesFor(new ItemStack(mat))){
                    if(r instanceof ShapedRecipe sr){
                        ItemStack ref = sr.getResult();
                        if(TiboiseItem.getItemId(ref).contains("_hammer")){
                            enchantmap.clear();
                            enchantmap.put(Enchantment.SILK_TOUCH,1);
                        }
                        if(TiboiseItem.getItemId(ref).contains("_broadaxe")){
                            enchantmap.clear();
                            enchantmap.put(Enchantment.SILK_TOUCH,1);
                        }
                        try{
                            // Adding unsafely here just to allow putting looting on axes
                            ref.addUnsafeEnchantments(enchantmap);
                        } catch (IllegalArgumentException e){
                            Main.logAdmin("ERROR", ref.getType()+" CANNOT HAVE ENCHANTEMENT SPECIFIED");
                        }
                        Bukkit.removeRecipe(sr.getKey());
                        ShapedRecipe ench = new ShapedRecipe(sr.getKey(),ref);
                        ench.setGroup(sr.getGroup());
                        ench.shape(sr.getShape());
                        for(Map.Entry<Character, RecipeChoice> entry : sr.getChoiceMap().entrySet()){
                            ench.setIngredient(entry.getKey(),entry.getValue());
                        }
                        Bukkit.addRecipe(ench);
                    }
                }
            }
            
            for(Recipe r : vanillabonusrecipes){
                try{
                    Bukkit.addRecipe(r);
                }catch (IllegalStateException e){
                    Main.logAdmin("ERROR", "Error in adding recipe for "+r.getResult().getType());
                }
            }
    
            // CRAFT FOR SPRUCE GLASS DOOR
            // cast to Keyed, not clean but should not break in future updates
            for(Recipe r : Bukkit.getRecipesFor(new ItemStack(Material.WARPED_DOOR))){
                if(r instanceof ShapedRecipe kd){
                    NamespacedKey key = kd.getKey();
                    ShapedRecipe doorcraft = new ShapedRecipe(key, new ItemStack(Material.WARPED_DOOR));
                    doorcraft.shape("WW","WG","WW")
                            .setIngredient('W',Material.SPRUCE_PLANKS)
                            .setIngredient('G', Material.GLASS_PANE)
                            .setGroup(kd.getGroup());
                    Bukkit.removeRecipe(key);
                    Bukkit.addRecipe(doorcraft);
                }
            }
            // CRAFT FOR SPRUCE GLASS TRAPDOOR
            // cast to Keyed, not clean but should not break in future updates
            for(Recipe r : Bukkit.getRecipesFor(new ItemStack(Material.WARPED_TRAPDOOR))){
                if(r instanceof ShapedRecipe kd){
                    NamespacedKey key = kd.getKey();
                    ShapedRecipe doorcraft = new ShapedRecipe(key, new ItemStack(Material.WARPED_TRAPDOOR));
                    doorcraft.shape("WGW","WWW")
                            .setIngredient('W',Material.SPRUCE_PLANKS)
                            .setIngredient('G', Material.GLASS_PANE)
                            .setGroup(kd.getGroup());
                    Bukkit.removeRecipe(key);
                    Bukkit.addRecipe(doorcraft);
                }
            }
            // CRAFT FOR OAK GLASS DOOR
            // cast to Keyed, not clean but should not break in future updates
            for(Recipe r : Bukkit.getRecipesFor(new ItemStack(Material.CRIMSON_DOOR))){
                if(r instanceof ShapedRecipe kd){
                    NamespacedKey key = kd.getKey();
                    ShapedRecipe doorcraft = new ShapedRecipe(key, new ItemStack(Material.CRIMSON_DOOR));
                    doorcraft.shape("WW","WG","WW")
                            .setIngredient('W',Material.OAK_PLANKS)
                            .setIngredient('G', Material.GLASS_PANE)
                            .setGroup(kd.getGroup());
                    Bukkit.removeRecipe(key);
                    Bukkit.addRecipe(doorcraft);
                }
            }
            // CRAFT FOR OAK GLASS TRAPDOOR
            // cast to Keyed, not clean but should not break in future updates
            for(Recipe r : Bukkit.getRecipesFor(new ItemStack(Material.CRIMSON_TRAPDOOR))){
                if(r instanceof ShapedRecipe kd){
                    NamespacedKey key = kd.getKey();
                    ShapedRecipe doorcraft = new ShapedRecipe(key, new ItemStack(Material.CRIMSON_TRAPDOOR));
                    doorcraft.shape("WGW","WWW")
                            .setIngredient('W',Material.OAK_PLANKS)
                            .setIngredient('G', Material.GLASS_PANE)
                            .setGroup(kd.getGroup());
                    Bukkit.removeRecipe(key);
                    Bukkit.addRecipe(doorcraft);
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.LOW)
    public static void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe r = it.next();
            if(r instanceof Keyed k && !p.hasDiscoveredRecipe(k.getKey())){
                p.discoverRecipe(k.getKey());
            }
            
        }
        
        // add the player to the team
        Main.addPlayerToInvisibleNametagTeam(p);
        
        // rename. TODO : merge this with the nickname mechanic
        String name = null;
        switch(p.getUniqueId().toString()){
            case "66e25a14-b468-4cb1-8cde-6cf6054255ba" -> name = "Gros Orteil de Pied";
            case "30b80f6f-f0dc-4b4a-96b2-c37b28494b1b" -> name = "MOLE1283";
            case "6864eb4a-91d6-4292-8dfb-f398cbd5dc57" -> name = "Walid Bedouin";
        }
        if(name != null){
            Component compname = Component.text(name);
            p.displayName(compname);
            p.playerListName(compname);
        }
    }
    
    //
    // ARMOR STANDS WITH HANDS BY DEFAULT
    @EventHandler(priority = EventPriority.LOW)
    public void placedArmorStandsWithArms(CreatureSpawnEvent event){
        if(event.getEntity() instanceof ArmorStand armst){
            armst.setArms(true);
        }
    }
    
    @EventHandler
    public void onPlayerUsesJukebox(PlayerInteractEvent event){
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.JUKEBOX && event.getAction().isRightClick() && event.getClickedBlock().getState() instanceof Jukebox j){
            final int DISK_ROTATION_PERIOD = 1;
            Block b = event.getClickedBlock();
            Location uploc = b.getLocation().clone().add(0.5,2,0.5);
            Player p = event.getPlayer();
            
            ItemStack disk = null;
            
            PlayerInventory pinv = p.getInventory();
            if(MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(pinv.getItemInMainHand().getType())){
                disk = pinv.getItemInMainHand().clone();
            } else if(MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(pinv.getItemInOffHand().getType())){
                disk = pinv.getItemInOffHand().clone();
            }
            
            if(j.isPlaying()){
                if((p.isSneaking() && p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) || (!p.isSneaking())){
                    for(Entity e : uploc.add(0,0.1,0).getNearbyEntities(0.1,0.25,0.1)){
                        if(e instanceof ArmorStand && ((ArmorStand) e).getEquipment().getHelmet() != null && ((ArmorStand) e).getEquipment().getHelmet().getType().toString().contains("MUSIC_DISC")){
                            e.remove();
                        }
                    }
                }
                
            } else if(disk != null && MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(disk.getType())){
                ArmorStand display = (ArmorStand) uploc.getWorld().spawnEntity(uploc.subtract(0,17/16f,0), EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM);
                display.setVisible(false);
                display.setInvulnerable(true);
                display.setGravity(false);
                display.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
                display.getEquipment().setHelmet(disk);
                display.setCustomNameVisible(false);
                display.customName(Component.text("_display"));
                display.setSmall(true);
                
                new BukkitRunnable(){
                    
                    final int MAX_MUSIC_DURATION = 3600;
                    int timer = MAX_MUSIC_DURATION;
                    final Location blockloc = b.getLocation().clone();
                    final float step = (40f/(20*DISK_ROTATION_PERIOD));
                    @Override
                    public void run(){
                        
                        if(timer <= 0){
                            this.cancel();
                        } else{
                            if(blockloc.getBlock().getType() != Material.JUKEBOX){
                                display.remove();
                                this.cancel();
                            }
                            display.setRotation(display.getLocation().getYaw()+step, 0);
                            timer--;
                            
                        }
                    }
                    
                }.runTaskTimer(Main.getPlugin(), 0, DISK_ROTATION_PERIOD);
            }
            
            
            
            
        }
    }
    
    @EventHandler
    public void onPlayerBreakJukebox(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.JUKEBOX){
            for(Entity e : event.getBlock().getLocation().add(0.5,1.1,0.5).getNearbyEntities(0.1,0.25,0.1)){
                if(e instanceof ArmorStand armst && armst.getEquipment().getHelmet() != null && MaterialSetTag.ITEMS_MUSIC_DISCS.isTagged(armst.getEquipment().getHelmet().getType())){
                    e.remove();
                }
            }
        }
    }
    
    
    /**
     * Used to apply some subtle random pitch variations to a sound
     * @return      a random pitch between 0.8 and 1.2 (float)
     */
    public static float randomPitchSimple(){
        return (float) ((new Random().nextDouble() * 0.4) + 0.8);
    }
}
