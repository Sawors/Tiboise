package io.github.sawors.tiboise;

import org.bukkit.NamespacedKey;

public interface UtilityBlock {
    static NamespacedKey utilityBlockKey = new NamespacedKey(Tiboise.getPlugin(),"utility-block");
    
    public String getUtilityIdentifier();
}
