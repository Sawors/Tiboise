package io.github.sawors.tiboise.painting;

import io.github.sawors.tiboise.ConfigModules;
import io.github.sawors.tiboise.Tiboise;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

public class PaintingHandler implements Listener {



    @EventHandler
    public static void onPlayerInteractWithCanvas(PlayerInteractEvent event){
        Player p = event.getPlayer();
        Vector view = p.getLocation().getDirection();
        int maxdistance = Tiboise.getModuleSection(ConfigModules.PAINTING) != null && Tiboise.getModuleSection(ConfigModules.PAINTING).getInt("brush-distance") > 0 ? Tiboise.getModuleSection(ConfigModules.PAINTING).getInt("brush-distance") : 16;
        maxdistance = 16;
        Entity e = p.getTargetEntity(maxdistance);
        if(e instanceof ItemFrame frame && frame.getItem().getType() == Material.MAP){
            ItemStack mapitem =  frame.getItem();
            MapMeta meta = (MapMeta) mapitem.getItemMeta();
            MapView mapview = meta.getMapView();
            Tiboise.logAdmin("frame");
            if(mapview != null){
                mapview.setUnlimitedTracking(false);

                // detecting the place where the player view collides with the bounding box
                Location collide = null;
                double distance = new Location(p.getPlayer().getWorld() ,e.getBoundingBox().getCenter().getX(),e.getBoundingBox().getCenter().getY(),e.getBoundingBox().getCenter().getZ()).distance(p.getLocation().add(0,1,0));
                Location origin = p.getEyeLocation().add(view.clone().normalize().multiply(distance));
                float stepvalue = .05f;
                Vector stepvector = view.clone().normalize().multiply(stepvalue);
                Tiboise.logAdmin("mapview");
                for(int i = 0; i<=1; i+= 1/stepvalue){
                    Location checkloc = origin.add(stepvector);
                    Tiboise.logAdmin("checking "+checkloc.getX()+" "+checkloc.getY()+" "+checkloc.getZ());
                    if(e.getBoundingBox().contains(checkloc.getX(),checkloc.getY(),checkloc.getZ())){
                        collide = checkloc;
                        Tiboise.logAdmin("collide found (last check)");
                        break;
                    }
                }

                if(collide != null){
                    e.getWorld().spawnParticle(Particle.FLAME, e.getLocation(),32,0,0,0,1);
                }
            }
        }
    }
}
