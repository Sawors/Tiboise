package io.github.sawors.tiboise;

import java.util.Locale;

public enum ConfigModules {
    FISHING, BETTERVANILLA, PAINTING, ECONOMY;
    
    
    public String getName(){
        return this.toString().toLowerCase(Locale.ROOT);
    }
}
