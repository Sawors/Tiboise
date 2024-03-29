package io.github.sawors.tiboise.items.utility;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.items.ItemSerializer;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PackingScotch extends TiboiseItem implements Listener {
    
    public static final String packedBlockItemId = "packed_block";
    
    public PackingScotch(){
        setMaterial(Material.SLIME_BALL);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setLore(List.of(
                Component.text("Use it to pack storage blocks.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Packed storage blocks can be broken").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("and moved without losing their content.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
    }
    
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void packBlock(PlayerInteractEvent event){
        
        ItemStack src = event.getPlayer().getInventory().getItemInMainHand();
        final Block clicked = event.getClickedBlock();
        if(TiboiseItem.getItemId(src).equals(getId(PackingScotch.class)) && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)){
            //event.setCancelled(true);
            // packing the block
            if(event.getPlayer().getCooldown(Material.SLIME_BALL) > 0) return;
            if(clicked != null && (clicked.getType().equals(Material.CHEST) || clicked.getType().equals(Material.BARREL)) && !(clicked.getState() instanceof DoubleChest)){
                Container container = (Container) clicked.getState();
                
                event.getPlayer().closeInventory();
                
                ItemStack packedBlockItem = new ItemStack(Material.PAPER);
                ItemMeta meta = packedBlockItem.getItemMeta();
                PersistentDataContainer c = meta.getPersistentDataContainer();
                c.set(TiboiseItem.getItemIdKey(), PersistentDataType.STRING,packedBlockItemId);
                StringBuilder typeskey = new StringBuilder();
                for(String s : Set.of(ItemTag.PREVENT_PACKING.toString(),ItemTag.PREVENT_USE_IN_CRAFTING.toString())){
                    typeskey.append(s.toUpperCase(Locale.ROOT)).append(":");
                }
                if(typeskey.toString().endsWith(":")){
                    typeskey.deleteCharAt(typeskey.lastIndexOf(":"));
                }
                c.set(TiboiseItem.getItemTagsKey(), PersistentDataType.STRING, typeskey.toString());
                c.set(getStoredMaterialKey(),PersistentDataType.STRING,container.getType().toString());
                
                meta.displayName(Component.text("Packed "+TiboiseUtils.capitalizeFirstLetter(clicked.getType().toString())).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                Map<String,Integer> contentSummary = new HashMap<>();
                List<ItemStack> excluded = new ArrayList<>();
                
                for(ItemStack i : container.getInventory().getContents()){
                    if(i != null && TiboiseItem.getItemTags(i).contains(ItemTag.PREVENT_PACKING.toString())){
                        excluded.add(i.clone());
                        i.setAmount(0);
                    }
                }
                // serialize content
                String serializedInventory = new ItemSerializer().serializeInventory(container.getInventory());
                
                for(ItemStack item : container.getSnapshotInventory().getContents()){
                    if(item == null) continue;
                    final String tiboiseId = TiboiseItem.getItemId(item);
                    contentSummary.put(tiboiseId,contentSummary.getOrDefault(tiboiseId,0)+item.getAmount());
                }
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Contains :").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                for(Map.Entry<String,Integer> line : contentSummary.entrySet()){
                    lore.add(Component.text(" - "+TiboiseUtils.capitalizeFirstLetter(line.getKey()).replaceAll("_"," ")+" "+line.getValue()+"x").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                final int maxLoreSize = 10;
                if(lore.size() > maxLoreSize){
                    final int loreBaseSize = lore.size();
                    lore = lore.subList(0,maxLoreSize);
                    lore.add(Component.text("   ... and "+(loreBaseSize-maxLoreSize)+" more").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                
                meta.lore(lore);
                c.set(getStorageKey(),PersistentDataType.STRING,serializedInventory);
                packedBlockItem.setItemMeta(meta);
                Map<Integer,ItemStack> additional = event.getPlayer().getInventory().addItem(packedBlockItem);
                for(ItemStack overflow : additional.values()){
                    event.getPlayer().getLocation().getWorld().dropItem(event.getPlayer().getLocation(),overflow);
                }
                
                final Location center = clicked.getLocation().add(.5,.5,.5);
                center.getWorld().playSound(center,clicked.getBlockSoundGroup().getBreakSound(),1,1);
                center.getWorld().playSound(center, Sound.BLOCK_SLIME_BLOCK_PLACE,.5f,1);
                center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,32,.25,.25,.25,.05,clicked.getBlockData());
                center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,8,.25,.25,.25,.05,Bukkit.createBlockData(Material.SLIME_BLOCK));
                
                for(ItemStack i : excluded){
                    center.getWorld().dropItem(center,i);
                }
                
                clicked.setType(Material.AIR);
                
                src.setAmount(src.getAmount()-1);
                event.getPlayer().setCooldown(Material.SLIME_BALL,10);
            }
        } else if(TiboiseItem.getItemId(src).equals(packedBlockItemId) && clicked != null && clicked.isSolid() && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)){
            //event.setCancelled(true);
            if(event.getPlayer().getCooldown(Material.PAPER) > 0) return;
            // placing back the packed block
            final BlockFace face = event.getBlockFace();
            Block toReplace = clicked.getRelative(face);
            new BukkitRunnable(){
                @Override
                public void run() {
                    event.getPlayer().closeInventory();
                    try{
                        
                        Map<Integer,ItemStack> content = new ItemSerializer().deserialize(Objects.requireNonNull(src.getItemMeta().getPersistentDataContainer().get(getStorageKey(), PersistentDataType.STRING)));
                        
                        toReplace.setType(Material.valueOf(src.getItemMeta().getPersistentDataContainer().get(getStoredMaterialKey(),PersistentDataType.STRING)));
                        if(toReplace.getBlockData() instanceof Directional directional){
                            
                            BlockFace facing = event.getPlayer().getFacing().getOppositeFace();
                            if(directional.getFaces().contains(facing)) directional.setFacing(facing);
                            toReplace.setBlockData(directional);
                        }
                        Inventory inv = ((Container) toReplace.getState()).getInventory();
                        for(Map.Entry<Integer,ItemStack> entry : content.entrySet()){
                            int slot = entry.getKey();
                            if(slot >= inv.getSize()) break;
                            inv.setItem(slot,entry.getValue());
                        }
                        
                        final Location center = toReplace.getLocation().add(.5,.5,.5);
                        center.getWorld().playSound(center,toReplace.getBlockSoundGroup().getPlaceSound(),1,1);
                        center.getWorld().playSound(center, Sound.BLOCK_SLIME_BLOCK_BREAK,.5f,1);
                        center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,12,.45,.45,.45,.05,Bukkit.createBlockData(Material.SLIME_BLOCK));
                        center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,8,.45,.45,.45,.05,Bukkit.createBlockData(Material.WHITE_CONCRETE));
                        
                        src.setAmount(src.getAmount()-1);
                        
                        
                    } catch (IllegalArgumentException | NullPointerException exception){exception.printStackTrace();}
                    event.getPlayer().closeInventory();
                    event.getPlayer().setCooldown(Material.PAPER,10);
                }
            }.runTask(Tiboise.getPlugin());
        }
    }
    
    private static NamespacedKey getStorageKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"packed_block_storage");
    }
    private static NamespacedKey getStoredMaterialKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"packed_block_material");
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(Tiboise.getPlugin(),this.getId()), this.get());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.SLIME_BALL);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
