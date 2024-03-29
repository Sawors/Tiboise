package io.github.sawors.tiboise.economy.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TradingStationOptions {
    boolean exactMatch;
    Set<UUID> allowedAccess;
    boolean showDisplay;
    
    public TradingStationOptions(){
        this.exactMatch = true;
        this.allowedAccess = new HashSet<>();
        this.showDisplay = true;
    }
    
    public String serialize(){
        try{
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e){
            return "{}";
        }
    }
    
    public static @Nullable TradingStationOptions deserialize(String serialized) {
        if(serialized == null) return null;
        try {
            return new ObjectMapper().readValue(serialized, TradingStationOptions.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    public boolean isExactMatch() {
        return exactMatch;
    }
    
    protected void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }
    
    public Set<UUID> getAllowedAccess() {
        return allowedAccess;
    }
    
    protected void setAllowedAccess(Set<UUID> allowedAccess) {
        this.allowedAccess = allowedAccess;
    }
    
    public boolean isShowDisplay() {
        return showDisplay;
    }
    
    protected void setShowDisplay(boolean showDisplay) {
        this.showDisplay = showDisplay;
    }
}
