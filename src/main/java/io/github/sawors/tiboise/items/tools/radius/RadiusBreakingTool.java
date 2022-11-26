package io.github.sawors.tiboise.items.tools.radius;

import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class RadiusBreakingTool extends TiboiseItem {
    
    ;
    
    // TODO :
    //  Add a correct 3D rotation to this method, currently it works but only out of pure luck
    protected static void radiusBreak(RadiusType type, int radius, Block origin, BlockFace minedface, ItemStack tool, double durabilitymultiplier){
        radiusBreak(type,radius,origin,minedface,tool,durabilitymultiplier,null);
    }
    protected static void radiusBreak(RadiusType type, int radius, Block origin, BlockFace minedface, ItemStack tool, double durabilitymultiplier, @Nullable Player breaker){
        Location center = origin.getLocation().add(.5,.5,.5);
        Vector direction = minedface.getDirection().multiply(-1);
        ArrayList<Vector> vectors = new ArrayList<>();
        //ArrayList<Vector> vectors2 = new ArrayList<>();
        
        switch (type){
            case SQUARE -> {
                for(int h = -radius; h<=radius;h++){
                    for(int w = -radius; w<=radius;w++){
                        vectors.add(new Vector(h,w,0));
                        //Tiboise.logAdmin(h+" : "+w);
                    }
                }
                /*for(int i = 1; i<=3;i++){
                    vectors2.add(direction.clone().multiply(i));
                }*/
            }
        }
        
        int damage = 0;
        for(Vector v : vectors){
            if(v.getZ() == 0 && v.getX() == 0 && v.getY() == 0){
                continue;
            }
            v.rotateAroundX((Math.PI/2)*direction.getY());
            v.rotateAroundY((Math.PI/2)*direction.getX());
            // we ignore Z here because by default Z is the direction
            Block b = center.getWorld().getBlockAt((int) (origin.getX()+v.getX()), (int) (origin.getY()+v.getY()), (int) (origin.getZ()+v.getZ()));
            if(b.getDestroySpeed(tool,false) > 1){
                b.breakNaturally(tool,true);
                damage++;
                /*new BukkitRunnable(){
                    @Override
                    public void run() {
                    
                    }
                }.runTask(Tiboise.getPlugin());*/
            }
            
        }
        
        if(tool.getItemMeta() instanceof Damageable d && !d.isUnbreakable() && breaker != null){
            tool.damage((int)(damage/durabilitymultiplier),breaker);
        }
        
        /*for(Vector v : vectors2){
            v.rotateAroundX((Math.PI/2)*direction.getY());
            v.rotateAroundY((Math.PI/2)*direction.getX());
            // we ignore Z here because by default Z is the direction
            center.getWorld().getBlockAt((int) (origin.getX()+v.getX()), (int) (origin.getY()+v.getY()), (int) (origin.getZ()+v.getZ())).setType(Material.GOLD_BLOCK);
        }*/
        //Tiboise.logAdmin("DIR : "+direction.getX()+" : "+direction.getY()+" : "+direction.getZ());
    }
}
