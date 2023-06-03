package io.github.sawors.tiboise.items.utility;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class PackingScotch extends TiboiseItem implements Listener {
    
    public PackingScotch(){
        setMaterial(Material.SLIME_BALL);
        setLore(List.of(
                Component.text("Use it to pack storage blocks.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Packed storage blocks can be broken").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("and moved without losing their content.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
    }
    
    @EventHandler
    public static void packBlock(PlayerInteractEvent event){
        
        final String packedBlockItemId = "packed_block";
        ItemStack src = event.getPlayer().getInventory().getItemInMainHand();
        final Block clicked = event.getClickedBlock();
        if(TiboiseItem.getItemId(src).equals(new PackingScotch().getId()) && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)){
            event.setCancelled(true);
            // packing the block
            if(clicked != null && (clicked.getType().equals(Material.CHEST) || clicked.getType().equals(Material.BARREL))){
                logAdmin("indeed");
                Container container = (Container) clicked.getState();
                
                ItemStack packedBlockItem = new ItemStack(Material.PAPER);
                ItemMeta meta = packedBlockItem.getItemMeta();
                PersistentDataContainer c = meta.getPersistentDataContainer();
                c.set(TiboiseItem.getItemIdKey(), PersistentDataType.STRING,packedBlockItemId);
                c.set(TiboiseItem.getItemTagsKey(), PersistentDataType.STRING,"prevent_use_in_crafting");
                c.set(getStoredMaterialKey(),PersistentDataType.STRING,container.getType().toString());
                
                // serialize content
                StringBuilder contentString = new StringBuilder();
                ItemStack[] content = container.getSnapshotInventory().getContents();
                int slot = 0;
                for(ItemStack item : content){
                    slot++;
                    if(item == null) continue;
                    contentString
                            .append("{")
                            .append("(").append(slot-1).append(")")
                            .append(Arrays.toString(item.serializeAsBytes()))
                            .append("},")
                    ;
                }
                contentString.deleteCharAt(contentString.length()-1);
                logAdmin(contentString);
                c.set(getStorageKey(),PersistentDataType.STRING,contentString.toString());
                packedBlockItem.setItemMeta(meta);
                event.getPlayer().getInventory().addItem(packedBlockItem);
                
                clicked.breakNaturally(false);
                
                src.setAmount(src.getAmount()-1);
                
            }
        } else if(TiboiseItem.getItemId(src).equals(packedBlockItemId) && clicked != null && clicked.isSolid() && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)){
            
            // placing back the packed block
            logAdmin("trigger");
            logAdmin(" "+event.getAction());
            logAdmin(" "+event.getHand());
            logAdmin(" "+event.getClickedBlock());
            logAdmin(" "+event.getItem());
            final BlockFace face = event.getBlockFace();
            Block toReplace = clicked.getRelative(face);
            try{
                final String storage = Objects.requireNonNull(src.getItemMeta().getPersistentDataContainer().get(getStorageKey(), PersistentDataType.STRING));
                
                Map<Integer,ItemStack> content = new HashMap<>();
                
                // deserialize
                for(String itemString : Objects.requireNonNull(src.getItemMeta().getPersistentDataContainer().get(getStorageKey(), PersistentDataType.STRING)).split("\\{")){
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
                
                toReplace.setType(Material.valueOf(src.getItemMeta().getPersistentDataContainer().get(getStoredMaterialKey(),PersistentDataType.STRING)));
                if(toReplace.getBlockData() instanceof Directional directional){
                    
                    BlockFace facing = event.getPlayer().getFacing().getOppositeFace();
                    if(directional.getFaces().contains(facing)) directional.setFacing(facing);
                    toReplace.setBlockData(directional);
                }
                Inventory inv = ((Container) toReplace.getState()).getInventory();
                for(Map.Entry<Integer,ItemStack> entry : content.entrySet()){
                    inv.setItem(entry.getKey(),entry.getValue());
                }
                src.setAmount(0);
                
                
            } catch (IllegalArgumentException | NullPointerException exception){exception.printStackTrace();}
        }
    }
    
    private static NamespacedKey getStorageKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"packed_block_storage");
    }
    private static NamespacedKey getStoredMaterialKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"packed_block_material");
    }
    
    public static PersistentDataContainer retrieveStorageData(ItemStack packedBlock){
        return packedBlock.getItemMeta().getPersistentDataContainer().get(getStorageKey(),PersistentDataType.TAG_CONTAINER);
    }
}
