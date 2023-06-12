package io.github.sawors.tiboise;

import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class TiboiseUtils {
    private static List<BlockFace> sortedBlockFaceList;
    
    
    protected static void initialize(){
        ArrayList<BlockFace> possibleFaces = new ArrayList<>(List.of(BlockFace.values()));
        possibleFaces.remove(BlockFace.UP);
        possibleFaces.remove(BlockFace.DOWN);
        possibleFaces.remove(BlockFace.SELF);
        Vector reference = new Vector(0,0,-1);
        possibleFaces.sort(Comparator.comparingDouble(m -> Math.atan2(((m.getDirection().getX()*reference.getZ()) - (m.getDirection().getZ()*reference.getX())), m.getDirection().dot(reference))));
        // Collections.reverse() or Lists.reverse() ? Which is better here ?
        Collections.reverse(possibleFaces);
        sortedBlockFaceList = possibleFaces;
    }
    
    public static List<BlockFace> getSortedHorizontalBlockFaces(){
        return List.copyOf(sortedBlockFaceList);
    }
    
    /**
     *
     * @param inventory The inventory from where the item is taken
     * @param item The item to be consumed, currently only works for vanilla and tiboise items
     */
    public static boolean consumeItemInInventory(Inventory inventory, ItemStack item){
        ItemStack consumable = null;
        int index = 0;
        ItemStack[] content = inventory.getStorageContents();
        for(int i = 0; i < inventory.getSize(); i++){
            ItemStack slotItem = inventory.getItem(i);
            if(slotItem != null && TiboiseItem.getItemId(content[i]).equals(TiboiseItem.getItemId(item))){
                inventory.setItem(i, slotItem.asQuantity(slotItem.getAmount()-1));
                return true;
            }
        }
        return false;
    }
    
    public static List<String> getPacketContentPrint(PacketContainer packet){
        List<String> output = new ArrayList<>();
        if(packet.getStructures() != null){
            try{
                for(InternalStructure structure : packet.getStructures().getValues()){
                    
                    if(structure.getDataWatcherModifier().size() > 0){
                        Tiboise.logAdmin("WATCHERS FOUND");
                        for(int i = 0; i<structure.getDataWatcherModifier().getValues().size(); i++){
                            structure.getDataWatcherModifier().getValues().get(i).asMap().forEach(((integer, wrappedWatchableObject) -> {
                                output.add(integer+" : "+wrappedWatchableObject);
                            }));
                        }
                    } else if(structure.getAttributeCollectionModifier().size() > 0){
                        Tiboise.logAdmin("W COLLECTION FOUND");
                        for(int i = 0; i<structure.getAttributeCollectionModifier().getValues().size(); i++){
                            for(int i2 = 0; i<structure.getAttributeCollectionModifier().getFields().size(); i2++){
                                structure.getAttributeCollectionModifier().readSafely(i2).forEach(w -> output.add(w.toString()));
                            }
                        }
                    }else {
                        //output.add(structure.toString());
                        //output.add(packet.getEN);
                    }
                    
                }
            } catch (NullPointerException e){
                output.add("null");
            }
        }
        return output;
    }
    
    public static String capitalizeFirstLetter(String text){
        if(text.contains(" ") || text.contains("_")){
            final String separator = text.contains("_") ? "_" : " ";
            StringBuilder bd = new StringBuilder();
            for(String s : text.split(separator)){
                if(s.length() > 0){
                    bd.append(s.substring(0, 1).toUpperCase(Locale.ROOT)).append(s.substring(1).toLowerCase(Locale.ROOT)).append(separator);
                }
            }
            bd.deleteCharAt(bd.length()-1);
            return bd.toString();
        }
        return text.substring(0,1).toUpperCase(Locale.ROOT)+text.substring(1).toLowerCase(Locale.ROOT);
    }
    
    public static String extractContent(Component source){
        StringBuilder builder = new StringBuilder(((TextComponent) source).content());
        for(Component c : source.children()){
            builder.append(((TextComponent) c).content());
        }
        return builder.toString();
    }
}
