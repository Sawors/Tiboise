package io.github.sawors.tiboise.agriculture;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class AnimalsManager implements Listener {
    @EventHandler
    public static void duckChance(EntityBreedEvent event){
        Entity e = event.getBreeder();
        if(e instanceof Chicken && (event.getMother().getName().equalsIgnoreCase("duck") || event.getFather().getName().equalsIgnoreCase("duck")) && Math.random() >= .01){
            event.getEntity().customName(Component.text("Duck"));
        }
    }
}
