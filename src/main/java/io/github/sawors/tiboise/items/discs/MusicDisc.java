package io.github.sawors.tiboise.items.discs;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class MusicDisc extends TiboiseItem {
    String music;
    String author;
    NamespacedKey musicKey;
    
    public MusicDisc(String musicName){
        this("unknown",musicName);
    }
    
    public MusicDisc(String author, String musicName){
        this.music = musicName;
        this.author = author;
        this.musicKey = new NamespacedKey(NamespacedKey.MINECRAFT,getKey());
        setLore(List.of(
                Component.text(getTitle()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
        addData(getMusicDataKey(),musicKey.asString());
        addData(getAuthorKey(),author);
        addData(getNameKey(),musicName);
        
        setDisplayName(Component.text("Music Disc").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        
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
    static NamespacedKey getAuthorKey() {
        return new NamespacedKey(Tiboise.getPlugin(),"music-disc-author");
    }
    static NamespacedKey getNameKey() {
        return new NamespacedKey(Tiboise.getPlugin(),"music-disc-name");
    }
    
    String getTitle() {
        return  (author+" - "+music).toLowerCase(Locale.ROOT);
    }
    
    public static Pair<String,String> parseTitleString(String title){
        String editableTitle = title;
        String author;
        String name;
        int separator = editableTitle.indexOf("-");
        
        if(separator > -1 && separator+1<=editableTitle.length()) {
            if(editableTitle.charAt(separator-1) == ' ') editableTitle = editableTitle.replaceFirst(" -","-");
            if(editableTitle.charAt(editableTitle.indexOf("-")+1) == ' ') editableTitle = editableTitle.replaceFirst("- ","-");
            logAdmin(editableTitle);
            separator = editableTitle.indexOf("-");
            author = editableTitle.substring(0,separator).toLowerCase(Locale.ROOT);
            name = editableTitle.substring(separator+1).toLowerCase(Locale.ROOT);
            
            return Pair.of(author,name);
        } else {
            return Pair.of("unknown","unknown");
        }
    }
    
    public String getKey(){
        return "tiboise.music_disc."+getTitleHash();
    }
    
    public int getTitleHash(){
        return getTitle().toLowerCase(Locale.ROOT).hashCode();
    }
}
