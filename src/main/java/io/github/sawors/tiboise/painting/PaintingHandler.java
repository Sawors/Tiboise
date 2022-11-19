package io.github.sawors.tiboise.painting;

import io.github.sawors.tiboise.Main;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PaintingHandler implements Listener {
    
    @EventHandler
    public static void paintItemFrameInteractions(PlayerInteractEntityEvent event){
        Player p = event.getPlayer();
        Vector view = p.getLocation().getDirection();
    
        Entity e = event.getRightClicked();
    
        // TOTEST
        //  CHECK if this correctly works for the ray tracing method and the collisions calculator
        //  CHECK if the collision point is correctly defined and if we can spawn particles at it, later we'll have to
        //  calculate what this ray traced collision spot corresponds to in terms of pixel
    
        if(e instanceof ItemFrame iframe && iframe.getItem().getType() == Material.FILLED_MAP){
            event.setCancelled(true);
            Location collide = getCollisionLocation(iframe, view,p);
            if(collide != null){
                // in this check we should do everything with the collision point, at his point we're sure it has been found.
                // The next step is now to translate this collision point to relatives coordinates relatively with the item frame origin
                // and translate this relative offset into pixel/render coordinates.
                e.getWorld().spawnParticle(Particle.REDSTONE, collide,32,0,0,0,0,new Particle.DustOptions(Color.RED, .5f));
            }
        }
    }
    
    
    public static @Nullable Location getCollisionLocation(ItemFrame frame, Vector viewline, Player source){
    
        Location collide = null;
        
        ItemStack mapitem =  frame.getItem();
        MapMeta meta = (MapMeta) mapitem.getItemMeta();
        MapView mapview = meta.getMapView();
        Main.logAdmin("frame");
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
            mapview.setTrackingPosition(false);
            mapview.setLocked(true);
    
            mapview.setScale(MapView.Scale.FARTHEST);
            mapview.getRenderers().clear();
    
            mapview.addRenderer(new MapRenderer() {
                @Override
                public void render(@NotNull MapView mapview, @NotNull MapCanvas mapcanvas, @NotNull Player player) {
                    //code goes here
            
                    // to draw text you do this
                    Image img = new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
                    Graphics gph = img.getGraphics();
                    gph.setColor(java.awt.Color.WHITE);
                    gph.fillRect(0,0,256,256);
                    mapcanvas.drawImage(0,0,img);
                }
            });
            
            // detecting the place where the player view collides with the bounding box
            double distance = new Location(source.getWorld(), frame.getBoundingBox().getCenter().getX(),frame.getBoundingBox().getCenter().getY(),frame.getBoundingBox().getCenter().getZ()).distance(source.getLocation().add(0,1,0));
            Location origin = source.getEyeLocation().add(viewline.clone().normalize().multiply(distance-.25));
            double stepvalue = 1/32f;
            Vector stepvector = viewline.clone().normalize().multiply(stepvalue);
            Main.logAdmin("mapview");
            for(double i = 0; i<=.5; i+= stepvalue){
                Location checkloc = origin.add(stepvector);
                Main.logAdmin("checking "+checkloc.getX()+" "+checkloc.getY()+" "+checkloc.getZ());
                if(canvasbox.contains(checkloc.getX(),checkloc.getY(),checkloc.getZ())){
                    collide = checkloc;
                    Main.logAdmin("collide found (last check)");
                    break;
                }
            }
            
            
        }
        return collide;
    }
}
