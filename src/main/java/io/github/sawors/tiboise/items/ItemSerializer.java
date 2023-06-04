package io.github.sawors.tiboise.items;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ItemSerializer {
    
    public String serialize(ItemStack item, int slot){
        if(item == null) return null;
        return "{" +
                "(" + (slot - 1) + ")" +
                Arrays.toString(item.serializeAsBytes()) +
                "},";
    }
    
    public String serialize(ItemStack item){
        return serialize(item,0);
    }
    
    public Map<Integer,ItemStack> deserialize(String serializedItems){
        Map<Integer,ItemStack> content = new HashMap<>();

        // deserialize
        for(String itemString : serializedItems.split("\\{")){
            if(itemString.length() == 0) continue;
            // byte array of each item
            String cleanedString = itemString.substring(itemString.indexOf("\\{")+1,itemString.lastIndexOf("}"));
            final int slot = Integer.parseInt(cleanedString.substring(cleanedString.indexOf("(")+1,cleanedString.indexOf(")")));
            cleanedString = cleanedString.substring(4);
            String[] itemBytesString = cleanedString.split(",");
            byte[] itemBytes = new byte[itemBytesString.length];
            for(int i = 0; i<itemBytesString.length; i++){
                itemBytes[i] = Integer.valueOf(itemBytesString[i].replaceAll(",","").replaceAll("\\[","").replaceAll("]","").replaceAll(" ","")).byteValue();
            }
            final ItemStack item = ItemStack.deserializeBytes(itemBytes);
            content.put(slot,item);
        }
        
        return content;
    }
    
    public ItemStack deserializeSingleItem(String serializedItem) {
        Map<Integer, ItemStack> fullContent = deserialize(serializedItem);
        if(fullContent.size() >= 1){
            return (ItemStack) fullContent.values().toArray()[0];
        } else {
            return null;
        }
    }
    
    public String serializeInventory(Inventory inv){
        ItemStack[] content = inv.getContents();
        StringBuilder contentString = new StringBuilder();
        for(int i = 0; i<content.length; i++){
            ItemStack item = content[i];
            if(item == null) continue;
            contentString.append(serialize(item,i));
        }
        return contentString.toString();
    }
    
    public Inventory deserializeInventory(String serializedInventory, Inventory targetInventory){
        Map<Integer,ItemStack> content = deserialize(serializedInventory);
        for(Map.Entry<Integer,ItemStack> entry : content.entrySet()){
            targetInventory.setItem(entry.getKey(),entry.getValue());
        }
        return targetInventory;
    }
    
    
    //final String packedBlockItemId = "packed_block";
    //        ItemStack src = event.getPlayer().getInventory().getItemInMainHand();
    //        final Block clicked = event.getClickedBlock();
    //        if(TiboiseItem.getItemId(src).equals(new PackingScotch().getId()) && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)){
    //            event.setCancelled(true);
    //            // packing the block
    //            if(clicked != null && (clicked.getType().equals(Material.CHEST) || clicked.getType().equals(Material.BARREL))){
    //                Container container = (Container) clicked.getState();
    //
    //                ItemStack packedBlockItem = new ItemStack(Material.PAPER);
    //                ItemMeta meta = packedBlockItem.getItemMeta();
    //                PersistentDataContainer c = meta.getPersistentDataContainer();
    //                c.set(TiboiseItem.getItemIdKey(), PersistentDataType.STRING,packedBlockItemId);
    //                c.set(TiboiseItem.getItemTagsKey(), PersistentDataType.STRING,"prevent_use_in_crafting");
    //                c.set(getStoredMaterialKey(),PersistentDataType.STRING,container.getType().toString());
    //
    //                meta.displayName(Component.text("Packed "+TiboiseUtils.capitalizeFirstLetter(clicked.getType().toString())).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    //                Map<String,Integer> contentSummary = new HashMap<>();
    //                // serialize content
    //                StringBuilder contentString = new StringBuilder();
    //                ItemStack[] content = container.getSnapshotInventory().getContents();
    //                int slot = 0;
    //                for(ItemStack item : content){
    //                    slot++;
    //                    if(item == null) continue;
    //                    final String tiboiseId = TiboiseItem.getItemId(item);
    //                    contentSummary.put(tiboiseId,contentSummary.getOrDefault(tiboiseId,0)+item.getAmount());
    //                    contentString
    //                            .append("{")
    //                            .append("(").append(slot-1).append(")")
    //                            .append(Arrays.toString(item.serializeAsBytes()))
    //                            .append("},")
    //                    ;
    //                }
    //                List<Component> lore = new ArrayList<>();
    //                lore.add(Component.text("Contains :").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    //                for(Map.Entry<String,Integer> line : contentSummary.entrySet()){
    //                    lore.add(Component.text(" - "+TiboiseUtils.capitalizeFirstLetter(line.getKey()).replaceAll("_"," ")+" "+line.getValue()+"x").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    //                }
    //                final int maxLoreSize = 10;
    //                if(lore.size() > maxLoreSize){
    //                    final int loreBaseSize = lore.size();
    //                    lore = lore.subList(0,maxLoreSize);
    //                    lore.add(Component.text("   ... and "+(loreBaseSize-maxLoreSize)+" more").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    //                }
    //
    //                meta.lore(lore);
    //
    //                contentString.deleteCharAt(contentString.length()-1);
    //                c.set(getStorageKey(),PersistentDataType.STRING,contentString.toString());
    //                packedBlockItem.setItemMeta(meta);
    //                Map<Integer,ItemStack> additional = event.getPlayer().getInventory().addItem(packedBlockItem);
    //                for(ItemStack overflow : additional.values()){
    //                    event.getPlayer().getLocation().getWorld().dropItem(event.getPlayer().getLocation(),overflow);
    //                }
    //
    //                final Location center = clicked.getLocation().add(.5,.5,.5);
    //                center.getWorld().playSound(center,clicked.getBlockSoundGroup().getBreakSound(),1,1);
    //                center.getWorld().playSound(center, Sound.BLOCK_SLIME_BLOCK_PLACE,1,1);
    //                center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,32,.25,.25,.25,.05,clicked.getBlockData());
    //                center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,8,.25,.25,.25,.05,Bukkit.createBlockData(Material.SLIME_BLOCK));
    //
    //                clicked.setType(Material.AIR);
    //
    //                src.setAmount(src.getAmount()-1);
    //
    //            }
    //        } else if(TiboiseItem.getItemId(src).equals(packedBlockItemId) && clicked != null && clicked.isSolid() && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)){
    //            event.setCancelled(true);
    //            // placing back the packed block
    //            final BlockFace face = event.getBlockFace();
    //            Block toReplace = clicked.getRelative(face);
    //            try{
    //
    //                Map<Integer,ItemStack> content = new HashMap<>();
    //
    //                // deserialize
    //                for(String itemString : Objects.requireNonNull(src.getItemMeta().getPersistentDataContainer().get(getStorageKey(), PersistentDataType.STRING)).split("\\{")){
    //                    if(itemString.length() == 0) continue;
    //                    // byte array of each item
    //                    String cleanedString = itemString.substring(itemString.indexOf("\\{")+1,itemString.lastIndexOf("}"));
    //                    final int slot = Integer.parseInt(cleanedString.substring(cleanedString.indexOf("(")+1,cleanedString.indexOf(")")));
    //                    cleanedString = cleanedString.substring(4);
    //                    String[] itemBytesString = cleanedString.split(",");
    //                    byte[] itemBytes = new byte[itemBytesString.length];
    //                    for(int i = 0; i<itemBytesString.length; i++){
    //                        itemBytes[i] = Integer.valueOf(itemBytesString[i].replaceAll(",","").replaceAll("\\[","").replaceAll("]","").replaceAll(" ","")).byteValue();
    //                    }
    //                    final ItemStack item = ItemStack.deserializeBytes(itemBytes);
    //                    content.put(slot,item);
    //                }
    //
    //                toReplace.setType(Material.valueOf(src.getItemMeta().getPersistentDataContainer().get(getStoredMaterialKey(),PersistentDataType.STRING)));
    //                if(toReplace.getBlockData() instanceof Directional directional){
    //
    //                    BlockFace facing = event.getPlayer().getFacing().getOppositeFace();
    //                    if(directional.getFaces().contains(facing)) directional.setFacing(facing);
    //                    toReplace.setBlockData(directional);
    //                }
    //                Inventory inv = ((Container) toReplace.getState()).getInventory();
    //                for(Map.Entry<Integer,ItemStack> entry : content.entrySet()){
    //                    inv.setItem(entry.getKey(),entry.getValue());
    //                }
    //
    //                final Location center = toReplace.getLocation().add(.5,.5,.5);
    //                center.getWorld().playSound(center,toReplace.getBlockSoundGroup().getPlaceSound(),1,1);
    //                center.getWorld().playSound(center, Sound.BLOCK_SLIME_BLOCK_BREAK,1,1);
    //                center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,12,.45,.45,.45,.05,Bukkit.createBlockData(Material.SLIME_BLOCK));
    //                center.getWorld().spawnParticle(Particle.BLOCK_DUST,center,8,.45,.45,.45,.05,Bukkit.createBlockData(Material.WHITE_CONCRETE));
    //
    //                src.setAmount(src.getAmount()-1);
    //
    //
    //            } catch (IllegalArgumentException | NullPointerException exception){exception.printStackTrace();}
    //        }
}
