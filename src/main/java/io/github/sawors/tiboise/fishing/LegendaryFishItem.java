package io.github.sawors.tiboise.fishing;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class LegendaryFishItem extends FishItem{

    Component name = Component.text("Unknown Legendary Fish").color(TextColor.color(0x555555));
    String uniquelore = "The mysterious unknown fish whose spawn occurs once every 30 bugs (THIS FISH SHOULD NEVER BE DROPPED, THIS IS A BUG)";

    public  LegendaryFishItem(String variant){

    }
}
