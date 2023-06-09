package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.NamespacedKey;

public abstract class UtilityEntity {
    
    public static NamespacedKey getUtilityEntityKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"utility-entity");
    }
    
    public abstract String getEntityIdentifier();
}
