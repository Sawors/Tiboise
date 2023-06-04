package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public abstract class SendableItem extends TiboiseItem {
    
    private UUID sender;
    private UUID receiver;
    private String content;
    private PostStamp stamp;
    
    public SendableItem(){
    
    }
    
    public static NamespacedKey getStampKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-stamp");
    }
    public static NamespacedKey getPathingKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-pathing");
    }
    
    public static NamespacedKey getContentKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content");
    }
}
