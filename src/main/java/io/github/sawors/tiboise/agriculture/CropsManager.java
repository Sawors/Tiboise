package io.github.sawors.tiboise.agriculture;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CropsManager implements Listener {
    
    private static boolean blacklistmode = true;
    private static List<String> croplist = new ArrayList<>();
    
    @EventHandler
    public static void preventBoneMealOnCrops(BlockFertilizeEvent event) {
        boolean contains = croplist.contains(event.getBlock().getType().toString());
        if ((blacklistmode && contains) || (!blacklistmode && !contains)) {
            event.setCancelled(true);
            event.getBlock().getWorld().spawnParticle(Particle.SMOKE_NORMAL,event.getBlock().getLocation().add(.5,2/16f,.5),16,.5,1/16f,.5,0);
        }
    }
    
    public static void loadBonemealList(){
        try(InputStream in = Tiboise.getPlugin().getResource("agriculture/bonemeal_list.yml")){
            if(in != null){
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                blacklistmode = Objects.equals(config.getString("mode"), "blacklist");
                config.getStringList("crops").forEach(c -> croplist.add(c.toUpperCase(Locale.ROOT)));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
