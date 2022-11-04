package io.github.sawors.tiboise.painting;

import io.github.sawors.tiboise.ConfigModules;
import io.github.sawors.tiboise.Tiboise;
import org.bukkit.Color;
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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class PaintingHandler implements Listener {



    @EventHandler
    public static void onPlayerInteractWithCanvas(PlayerInteractEvent event){
        Player p = event.getPlayer();
        Vector view = p.getLocation().getDirection();
        int maxdistance = Tiboise.getModuleSection(ConfigModules.PAINTING) != null && Tiboise.getModuleSection(ConfigModules.PAINTING).getInt("brush-distance") > 0 ? Tiboise.getModuleSection(ConfigModules.PAINTING).getInt("brush-distance") : 16;
        maxdistance = 16;
        Entity e = p.getTargetEntity(maxdistance);
        ItemFrame frame = null;
        RayTraceResult rt = p.rayTraceBlocks(maxdistance);
        Location hit = null;
        if(rt != null){
            hit = new Location((p.getWorld()),0,0,0).add(rt.getHitPosition());
        }

        Tiboise.logAdmin("interact");
        // TOTEST
        //  CHECK if this correctly works for the ray tracing method and the collisions calculator
        //  CHECK if the collision point is correctly defined and if we can spawn particles at it, later we'll have to
        //  calculate what this ray traced collision spot corresponds to in terms of pixel

        if(e instanceof ItemFrame iframe && iframe.getItem().getType() == Material.FILLED_MAP){
            frame = iframe;
        } else if (hit != null){
            for(ItemFrame checkframe : hit.getNearbyEntitiesByType(ItemFrame.class,2/16D)){
                frame = checkframe;
                break;
            }
        }

        if(frame != null){
            ItemStack mapitem =  frame.getItem();
            MapMeta meta = (MapMeta) mapitem.getItemMeta();
            MapView mapview = meta.getMapView();
            Tiboise.logAdmin("frame");
            BoundingBox sourcebox = frame.getBoundingBox();
            BoundingBox canvasbox = sourcebox.clone();

            // expanding the bounding box to accord with the "item frame with map" visual bounding box size
            double expansion = (1-(canvasbox.getMaxY()-canvasbox.getMinY()))/2;
            canvasbox.expand(0,expansion,0);
            if(canvasbox.getMaxX()-canvasbox.getMinX() >= .5){
                canvasbox.expand(expansion,0,0);
            } else if(canvasbox.getMaxZ()-canvasbox.getMinZ() >= .5) {
                canvasbox.expand(0,0,expansion);
            }
            if(mapview != null){
                mapview.setUnlimitedTracking(false);

                // detecting the place where the player view collides with the bounding box
                Location collide = null;
                double distance = new Location(p.getPlayer().getWorld(), frame.getBoundingBox().getCenter().getX(),frame.getBoundingBox().getCenter().getY(),frame.getBoundingBox().getCenter().getZ()).distance(p.getLocation().add(0,1,0));
                Location origin = p.getEyeLocation().add(view.clone().normalize().multiply(distance-.25));
                double stepvalue = 1/32f;
                Vector stepvector = view.clone().normalize().multiply(stepvalue);
                Tiboise.logAdmin("mapview");
                for(double i = 0; i<=1; i+= stepvalue){
                    Location checkloc = origin.add(stepvector);
                    Tiboise.logAdmin("checking "+checkloc.getX()+" "+checkloc.getY()+" "+checkloc.getZ());
                    if(canvasbox.contains(checkloc.getX(),checkloc.getY(),checkloc.getZ())){
                        collide = checkloc;
                        Tiboise.logAdmin("collide found (last check)");
                        break;
                    }
                }

                if(collide != null){
                    // in this check we should do everything with the collision point, at his point we're sure it has been found.
                    // The next step is now to translate this collision point to relatives coordinates relatively with the item frame origin
                    // and translate this relative offset into pixel/render coordinates.
                    frame.getWorld().spawnParticle(Particle.REDSTONE, collide,32,0.05,0.05,0.05,0,new Particle.DustOptions(Color.RED, .5f));
                }
            }
        }
    }
}
