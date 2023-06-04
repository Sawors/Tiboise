package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.NamespacedKey;

public abstract class SendableItem extends TiboiseItem {
    
    private PostStamp stamp;
    
    public SendableItem(){
    
    }
    
    public static NamespacedKey getStampKey() {
        return new NamespacedKey(Tiboise.getPlugin(), "post-stamp");
    }
    
    
    
    public PostStamp getStamp() {
        return stamp;
    }
    
    public void setStamp(PostStamp stamp) {
        this.stamp = stamp;
    }
}
