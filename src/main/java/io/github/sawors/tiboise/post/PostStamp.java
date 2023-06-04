package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;

public class PostStamp extends TiboiseItem {
    
    public PostStamp(){
        setMaterial(Material.PAPER);
    }
    
    
    public enum StampVariants {
        DEFAULT
    }
}
