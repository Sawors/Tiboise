package io.github.sawors.tiboise.integrations.voicechat;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class FrequencyItem extends TiboiseItem {
    double frequency = 462.6;
    
    
    public void setFrequency(double frequency){
        this.frequency = frequency;
    }
    public double getFrequency(){
        return frequency;
    }
    
    public static String getFrequencyDisplay(double frequency){
        return String.format("%,.1f",frequency);
    }
    
    public static double getItemFrequency(ItemStack item){
        double frequ = 0;
        if(item.hasItemMeta()){
            Double d = item.getItemMeta().getPersistentDataContainer().get(getFrequencyKey(), PersistentDataType.DOUBLE);
            if(d != null){
                frequ = d;
            }
        }
        return frequ;
    }
    
    public static void setItemFrequency(ItemStack item, double frequency){
        if(item.hasItemMeta()){
            item.getItemMeta().getPersistentDataContainer().set(getFrequencyKey(), PersistentDataType.DOUBLE,frequency);
        }
    }
    
    
    
    private static NamespacedKey getFrequencyKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "radio-frequency");
    }
}
