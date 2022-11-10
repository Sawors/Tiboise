package io.github.sawors.tiboise.fishing;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Material;

public class FishItem extends TiboiseItem {

    double weight = 0;
    FishAge age = FishAge.ADULT;


    public FishItem() {
        super();

        setMaterial(Material.COD);
        setVariant("cod");
    }

    public FishItem(String variant){
        super();

        setMaterial(Material.COD);
        setVariant(variant);
    }
}
