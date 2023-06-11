package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.NamespacedKey;

public interface UtilityEntity {
    
    public static NamespacedKey utilityKey = new NamespacedKey(Tiboise.getPlugin(),"utility-entity");
    
    public abstract String getEntityIdentifier();
}
