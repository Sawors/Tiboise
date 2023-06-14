package io.github.sawors.tiboise;

import org.bukkit.NamespacedKey;

public interface UtilityBlock {
    NamespacedKey utilityBlockKey = new NamespacedKey(Tiboise.getPlugin(),"utility-block");
    
    String getUtilityIdentifier();
}
