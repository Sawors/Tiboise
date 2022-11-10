package io.github.sawors.tiboise.fishing;

import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;


public class FishingManager implements Listener {

    Map<String, FishItem> fishmap = new HashMap<>();
    Map<String, LegendaryFishItem> legendaryfishmap = new HashMap<>();
    Map<String, String> watertypesmap = new HashMap<>();

    public static void loadFishVariants(){

    }

    public static void loadLegendaryFishVariants(){

    }

    public static void loadWaterZones(){

    }
}
