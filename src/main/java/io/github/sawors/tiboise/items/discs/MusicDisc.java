package io.github.sawors.tiboise.items.discs;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MusicDisc extends TiboiseItem {
    String music;
    NamespacedKey musicKey;
    
    public MusicDisc(String musicName){
        this.music = musicName;
        this.musicKey = new NamespacedKey(NamespacedKey.MINECRAFT,"tiboise.music_disc."+music.replaceAll(" ","_"));
        setLore(List.of(Component.text(musicName).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
        addData(getMusicDataKey(),musicKey.asString());
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setMaterial(Material.MUSIC_DISC_11);
    }
    
    public MusicDisc(){
        this("cat");
    }
    
    @Override
    public ItemStack get() {
        ItemStack s = super.get();
        
        s.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        
        return s;
    }
    
    static NamespacedKey getMusicDataKey() {
        return new NamespacedKey(Tiboise.getPlugin(),"music-disc-data");
    }
}
