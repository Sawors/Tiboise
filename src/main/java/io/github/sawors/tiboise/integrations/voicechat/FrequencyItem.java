package io.github.sawors.tiboise.integrations.voicechat;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FrequencyItem extends TiboiseItem {
    // IMPORTANT : Frequencies are multiplied by 1000, so 462.6 is here 462600
    private int frequency = 462600;
    
    
    public void setFrequency(int frequency){
        this.frequency = frequency;
    }
    public int getFrequency(){
        return frequency;
    }
    
    public static String getFrequencyDisplay(int frequency){
        return String.format("%,.2f",frequency/1000f);
    }
    
    public static int getItemFrequency(ItemStack item){
        int frequ = 0;
        if(item.hasItemMeta()){
            Integer d = item.getItemMeta().getPersistentDataContainer().get(getFrequencyKey(), PersistentDataType.INTEGER);
            if(d != null){
                frequ = d;
            }
        }
        return frequ;
    }
    
    public static void setItemFrequency(ItemStack item, int frequency){
        if(item.hasItemMeta()){
            ItemMeta m  = item.getItemMeta();
            m.getPersistentDataContainer().set(getFrequencyKey(), PersistentDataType.INTEGER,frequency);
            item.setItemMeta(m);
        }
    }
    
    @Override
    public ItemStack get() {
        ItemStack i = super.get();
        ItemMeta m  = i.getItemMeta();
        m.getPersistentDataContainer().set(getFrequencyKey(), PersistentDataType.INTEGER,getFrequency());
        i.setItemMeta(m);
        return i;
    }
    
    
    protected static NamespacedKey getFrequencyKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "radio-frequency");
    }
    
    
}
