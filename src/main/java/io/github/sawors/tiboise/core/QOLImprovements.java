package io.github.sawors.tiboise.core;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.core.local.LocalResourcesManager;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class QOLImprovements implements Listener {
    
    private final static boolean DO_CHAT_BUBBLES = true;
    private static Map<UUID, UUID> playerChatBubbles = new  HashMap<>();
    
    
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
                }.runTaskLater(Tiboise.getPlugin(), 1);
            }
        }
    }
    
    //
    // TORCH PUT THE TARGET ON FIRE
    @EventHandler
    public void setEntityOnFireWhenHitWithTorch(EntityDamageByEntityEvent event){
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
            }.runTask(Tiboise.getPlugin());
        }
    }
    
    //
    //  REMOVE XP EARNING
    @EventHandler
    public void removeXpEarning(PlayerExpChangeEvent event){
        event.setAmount(0);
        event.getPlayer().setLevel(0);
    }
    @EventHandler
    public void removeAdvancementXp(PlayerAdvancementDoneEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                event.getPlayer().setLevel(0);
                event.getPlayer().setExp(0);
            }
        }.runTaskLater(Tiboise.getPlugin(),1);
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
    public void onLoad(PluginEnableEvent event){
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            List<Recipe> vanillabonusrecipes = new ArrayList<>();
            
            // SADDLES
            vanillabonusrecipes.add(
                    new ShapedRecipe(Material.SADDLE.getKey(), new ItemStack(Material.SADDLE)).shape(
                            "LLL",
                            "SXS"
                    )
                    .setIngredient('X', Material.AIR)
                    .setIngredient('L', Material.LEATHER)
                    .setIngredient('S', Material.STRING)
            );
            // NAME TAGS
            vanillabonusrecipes.add(
                    new ShapelessRecipe(Material.NAME_TAG.getKey(), new ItemStack(Material.NAME_TAG))
                    .addIngredient(Material.GOLD_NUGGET)
                    .addIngredient(Material.PAPER)
            );
            // GLOWSTONE DUST
            vanillabonusrecipes.add(
                    new ShapelessRecipe(Material.GLOWSTONE_DUST.getKey(), new ItemStack(Material.GLOWSTONE_DUST).asQuantity(4))
                            .addIngredient(4, Material.FLINT)
                            .addIngredient(1,Material.GLOW_BERRIES)
            );
            // SHROOMLIGHT
            vanillabonusrecipes.add(
                    new ShapedRecipe(Material.SHROOMLIGHT.getKey(), new ItemStack(Material.SHROOMLIGHT))
                            .shape("XLX","LGL","XLX")
                            .setIngredient('L', new RecipeChoice.MaterialChoice(Tag.LEAVES))
                            .setIngredient('G',Material.GLOW_BERRIES)
                            .setIngredient('X',Material.AIR)
            );
            // GILDED BLACKSTONE
            vanillabonusrecipes.add(
                    new ShapedRecipe(Material.GILDED_BLACKSTONE.getKey(), new ItemStack(Material.GILDED_BLACKSTONE))
                            .shape("NNN","NBN","NNN")
                            .setIngredient('N', Material.GOLD_NUGGET)
                            .setIngredient('B',Material.BLACKSTONE)
            );
            // GUNPOWDER
            vanillabonusrecipes.add(
                    new ShapelessRecipe(Material.GUNPOWDER.getKey(), new ItemStack(Material.GUNPOWDER).asQuantity(1))
                            .addIngredient(2, Material.FLINT)
                            .addIngredient(1,Material.COAL)
            );
            
            vanillabonusrecipes.add(
                    new ShapelessRecipe(new NamespacedKey(Tiboise.getPlugin(),"calcite_quartz"), new ItemStack(Material.QUARTZ).asQuantity(2))
                            .addIngredient(1, Material.CALCITE)
            );
            
            vanillabonusrecipes.add(
                    new ShapelessRecipe(new NamespacedKey(Tiboise.getPlugin(),"decraft_prismarine"), new ItemStack(Material.PRISMARINE_SHARD).asQuantity(4))
                            .addIngredient(1, Material.PRISMARINE)
            );
            
            // MAKE GOLD ITEM ENCHANTED BY DEFAULT
            List<Material> goldtoedit = List.of(Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_AXE, Material.GOLDEN_SWORD);
            for (Material mat : goldtoedit){
                Map<Enchantment, Integer> enchantmap = new HashMap<>();
                switch (mat){
                    case GOLDEN_HOE, GOLDEN_SHOVEL -> enchantmap.put(Enchantment.SILK_TOUCH,1);
                    case GOLDEN_SWORD, GOLDEN_AXE -> enchantmap.put(Enchantment.LOOT_BONUS_MOBS,2);
                    case GOLDEN_PICKAXE -> enchantmap.put(Enchantment.LOOT_BONUS_BLOCKS,3);
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
                            logAdmin("ERROR", ref.getType()+" CANNOT HAVE ENCHANTEMENT SPECIFIED");
                        }
                        Bukkit.removeRecipe(sr.getKey());
                        ShapedRecipe ench = new ShapedRecipe(sr.getKey(),ref);
                        ench.setGroup(sr.getGroup());
                        ench.shape(sr.getShape());
                        ench.setCategory(CraftingBookCategory.EQUIPMENT);
                        for(Map.Entry<Character, RecipeChoice> entry : sr.getChoiceMap().entrySet()){
                            ench.setIngredient(entry.getKey(),entry.getValue());
                        }
                        Bukkit.addRecipe(ench);
                    }
                }
            }

            // DOUBLE THE AMOUNT OF IRON TRAPDOORS CRAFTED
            NamespacedKey trapKey = Material.IRON_TRAPDOOR.getKey();
            Bukkit.removeRecipe(trapKey);
            ShapedRecipe trapRecipe = new ShapedRecipe(trapKey, new ItemStack(Material.IRON_TRAPDOOR).asQuantity(2))
                    .shape("II","II").setIngredient('I',Material.IRON_INGOT);
            trapRecipe.setCategory(CraftingBookCategory.BUILDING);
            Bukkit.addRecipe(trapRecipe);
            
            for(Recipe r : vanillabonusrecipes){
                try{
                    Bukkit.addRecipe(r);
                }catch (IllegalStateException e){
                    logAdmin("ERROR", "Error in adding recipe for "+r.getResult().getType());
                }
            }
            
            // LODESTONE WITH IRON INSTEAD OF NETHERITE
            final NamespacedKey lodeKey = Material.LODESTONE.getKey();
            Bukkit.removeRecipe(lodeKey);
            ShapedRecipe lodeRecipe = new ShapedRecipe(Material.LODESTONE.getKey(),new ItemStack(Material.LODESTONE))
                    .shape(
                            "SSS",
                            "SIS",
                            "SSS"
                    )
                    .setIngredient('S', Material.SMOOTH_STONE)
                    .setIngredient('I',Material.IRON_INGOT);
            lodeRecipe.setCategory(CraftingBookCategory.BUILDING);
            Bukkit.addRecipe(lodeRecipe);
    
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
            
            // REMOVE DUPLICATE MAP RECIPE, NOW PLAYERS ARE REQUIRED TO USE THE CARTOGRAPHY TABLE
            for(Recipe r : Bukkit.getRecipesFor(new ItemStack(Material.FILLED_MAP))){
                if(r instanceof ShapedRecipe sr){
                    Bukkit.removeRecipe(sr.getKey());
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public static void allowGoldenPickaxesToBreakCopper(BlockBreakEvent event){
        final Set<Material> additionalMinable = Set.of(
                Material.COPPER_ORE
        );
        try{
            final Block block = event.getBlock();
            final ItemStack ref = event.getPlayer().getInventory().getItemInMainHand();
            if(ref.getType().equals(Material.GOLDEN_PICKAXE) && additionalMinable.contains(block.getType())){
                event.setCancelled(true);
                ItemStack fakePickaxe = new ItemStack(Material.IRON_PICKAXE);
                fakePickaxe.addEnchantments(ref.getEnchantments());
                block.breakNaturally(fakePickaxe, false);
                ref.damage(1,event.getPlayer());
            }
        } catch (NullPointerException ignored){}
    }
    
    private static final Map<UUID,Location> spawnSet = new HashMap<>();
    
    @EventHandler
    public void askForSpawnpointConfirmation(PlayerSetSpawnEvent event){
        
        int delay = 5;
        
        if(!event.isForced()){
            Player p = event.getPlayer();
            new BukkitRunnable(){
                @Override
                public void run() {
                    p.sendActionBar(Component.text("Do you really want to set your spawn here ?").color(NamedTextColor.YELLOW));
                }
            }.runTask(Tiboise.getPlugin());
            event.setCancelled(true);
            
            if(!spawnSet.containsKey(p.getUniqueId())){
                spawnSet.put(p.getUniqueId(),event.getLocation());
                p.sendMessage(Component.text("Click here to set your spawn").color(NamedTextColor.GREEN).clickEvent(ClickEvent.callback(f -> {
                    if(spawnSet.containsKey(p.getUniqueId())){
                        p.setBedSpawnLocation(event.getLocation(),true);
                        p.sendMessage(Component.text("Respawn point set"));
                        spawnSet.remove(p.getUniqueId());
                    } else {
                        p.sendMessage(Component.text("Delay expired").color(NamedTextColor.RED));
                    }
                })));
                
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        spawnSet.remove(p.getUniqueId());
                        p.sendMessage(Component.text("Delay expired").color(NamedTextColor.RED));
                    }
                }.runTaskLater(Tiboise.getPlugin(),delay*20);
            }
        }
    }
    
    //
    // SET GAMERULES
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        World w = event.getWorld();
        w.setGameRule(GameRule.DO_INSOMNIA, false);
        w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        //w.setGameRule(GameRule.NATURAL_REGENERATION, false);
        // wrench
    }
    
    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe r = it.next();
            if(r instanceof Keyed k && !p.hasDiscoveredRecipe(k.getKey())){
                p.discoverRecipe(k.getKey());
            }
            
        }
        
        // add the player to the team
        Tiboise.addPlayerToInvisibleNametagTeam(p);
        
        // rename. TODO : merge this with the nickname mechanic
        if(LocalResourcesManager.hasNickName(p.getUniqueId())){
            //switch(p.getUniqueId().toString()){
            //            case "66e25a14-b468-4cb1-8cde-6cf6054255ba" -> name = "Gros Orteil de Pied";
            //            case "30b80f6f-f0dc-4b4a-96b2-c37b28494b1b" -> name = "MOLE1283";
            //            case "6864eb4a-91d6-4292-8dfb-f398cbd5dc57" -> name = "Walid Bedouin";
            //        }
            final String name = LocalResourcesManager.getNickName(p.getUniqueId());
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
    public void removeAnvilDamage(AnvilDamagedEvent event){
        event.setCancelled(true);
    }
    
    @EventHandler
    public void removeAnvilCost(PrepareAnvilEvent event){
        if(event.getView().getTopInventory() instanceof AnvilInventory inv){
            inv.setRepairCost(0);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void prettyPlayerMessages(AsyncChatEvent event){
        TextComponent message = (TextComponent) event.message();
        String content = message.content();
        Component author = event.getPlayer().displayName();
        
        // overwrite default event content without cancelling it in order to allow usage by other methods
        if(!event.isCancelled()){
            for(Audience p : event.viewers()){
                p.sendMessage(author.append(Component.text(" : ")).color(NamedTextColor.GRAY).append(Component.text(content).color(NamedTextColor.WHITE)).clickEvent(ClickEvent.suggestCommand("/msg "+event.getPlayer().getName()+" ")));
            }
            event.viewers().clear();
            
            Player p = event.getPlayer();
            
            // Wrapping the generation of the text bubble in a runnable since we are in an async context
            final int period = 1;
            if(DO_CHAT_BUBBLES){
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(p.getLocation().getNearbyEntitiesByType(Player.class,16).size() < 2) return;
                        final int lineWidth = 78*2;
                        final int lineAmount = Math.min(1,content.length()/lineWidth);
                        final Vector displacement = new Vector(0,2+(lineAmount*.1),0);
                        TextDisplay bubble = (TextDisplay) p.getWorld().spawnEntity(p.getLocation().add(displacement),EntityType.TEXT_DISPLAY);
                        if(playerChatBubbles.containsKey(p.getUniqueId())){
                            Entity e = p.getWorld().getEntity(playerChatBubbles.get(p.getUniqueId()));
                            if(e != null) e.remove();
                        }
                        playerChatBubbles.put(p.getUniqueId(), bubble.getUniqueId());
                        bubble.setLineWidth(lineWidth);
                        bubble.text(message);
                        bubble.setBillboard(Display.Billboard.CENTER);
                        bubble.setSeeThrough(true);
                        bubble.getPersistentDataContainer().set(FloatingTextUtils.getTemporaryTagKey(), PersistentDataType.STRING,FloatingTextUtils.cleanupDisplayTag);
                        
                        final int max = Math.max((int) ((20.0*(content.replaceAll(" ","").length()/15.0))/period),30);
                        new BukkitRunnable(){
                            
                            
                            int count = 0;
                            
                            @Override
                            public void run() {
                                count++;
                                if(!bubble.isValid()){
                                    this.cancel();
                                    bubble.remove();
                                    return;
                                }
                                if(count >= max){
                                    this.cancel();
                                    bubble.remove();
                                    if(playerChatBubbles.containsKey(p.getUniqueId()) && playerChatBubbles.get(p.getUniqueId()).equals(bubble.getUniqueId())){
                                        Entity e = p.getWorld().getEntity(playerChatBubbles.get(p.getUniqueId()));
                                        if(e != null) e.remove();
                                        playerChatBubbles.remove(p.getUniqueId());
                                    }
                                    return;
                                }
                                
                                final Location pLoc = p.getLocation();
                                
                                bubble.teleport(pLoc.add(displacement));
                                /*bubble.getLocation().setYaw(pLoc.getYaw());
                                bubble.getLocation().setPitch(pLoc.getPitch());*/
                                
                                
                            }
                        }.runTaskTimer(Tiboise.getPlugin(),0,period);
                    }
                }.runTask(Tiboise.getPlugin());
            }
        }
        
    }
    
    
    
    @EventHandler
    public static void welcomeMessage(PlayerJoinEvent event){
        final Player p = event.getPlayer();
        Component message = Component.text("");
        String mapUrl = Tiboise.getMapUrl();
        if(mapUrl != null){
            message = message.append(
                    Component.text("Hello, welcome to Tiboise! You can have access to a world map using ")
                            .color(NamedTextColor.GOLD)
                    )
                    .append(
                            Component.text("this link")
                                    .color(NamedTextColor.GREEN)
                                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE)
                                    .hoverEvent(Component.text("> click to go to the website <").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE))
                                    .clickEvent(ClickEvent.openUrl(Tiboise.getMapUrl()))
                    ).append(Component.text(".").color(NamedTextColor.GOLD));
        }
        String version = Tiboise.getVersion();
        message = message.append(
                        Component.text("\nThe server is currently running Tiboise version "+Tiboise.getVersion()).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE)
                                .hoverEvent(Component.text("> click to read the patchnote <").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.TRUE))
                                .clickEvent(ClickEvent.openUrl("https://github.com/Sawors/Tiboise/releases/tag/v"+(version.indexOf(".") != version.lastIndexOf(".") ? version : version+".0")))
                );
        p.sendMessage(message);
    }
    
    @EventHandler
    public static void manageLowDurabilityItem(PlayerItemDamageEvent event){
        final Player p = event.getPlayer();
        final ItemStack item = event.getItem();
        final int maxDamage = item.getType().getMaxDurability();
        final int baseDamage = ((Damageable) item.getItemMeta()).getDamage();
        final int damage = event.getDamage();
        // the percentage of the items durability at which a warning should be sent
        final int warningThreshold = 15;
        final int threshold = (int) (maxDamage*((100-warningThreshold)/100.0));
        if(baseDamage+damage >= threshold && baseDamage < threshold) {
            final ItemMeta meta = item.getItemMeta();
            Component name = meta != null && meta.displayName() != null ? meta.displayName() : Component.text(TiboiseUtils.capitalizeFirstLetter(item.getType().toString().replaceAll("_"," ").toLowerCase(Locale.ROOT))).color(NamedTextColor.GREEN);
            name = name != null ? name : item.displayName();
            p.showTitle(Title.title(Component.text(""),Component.text("Your ").append(name).append(Component.text(" will break soon")).color(NamedTextColor.GOLD)));
            p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1.5f);
        }
        if(baseDamage+damage >= maxDamage){
            if(TiboiseItem.getItemTags(item).contains(ItemTag.PREVENT_BREAKING.toString())){
                event.setCancelled(true);
                p.playSound(p.getLocation(),Sound.ENTITY_ITEM_BREAK,1,1);
                final ItemStack copy = item.clone();
                item.setAmount(0);
                for(ItemStack overflow : p.getInventory().addItem(copy).values()){
                    p.getWorld().dropItem(p.getLocation(),overflow);
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
