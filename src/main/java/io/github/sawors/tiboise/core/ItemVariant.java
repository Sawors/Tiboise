package io.github.sawors.tiboise.core;

import io.github.sawors.tiboise.items.TiboiseItem;

public enum ItemVariant {
    //default
    DEFAULT,
    //wood types
    OAK, SPRUCE, DARK_OAK, BIRCH, ACACIA, JUNGLE, CRIMSON, WARPED,
    //metals and ores
    IRON, GOLD, DIAMOND, WOOD, STONE, EMERALD, LAPIS, REDSTONE, COAL, COPPER, AMETHYST,
    //flowers
    DANDELION, POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILLY_OF_THE_VALLEY
    ;

    public String getFormatted(){
        return TiboiseItem.formatTextToId(this.toString());
    }

}
