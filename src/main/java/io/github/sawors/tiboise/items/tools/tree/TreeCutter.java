package io.github.sawors.tiboise.items.tools.tree;

import com.destroystokyo.paper.MaterialSetTag;
import com.google.common.collect.Lists;
import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TreeCutter extends TiboiseItem {
    private int limit = 4;
    private CuttingMode mode = CuttingMode.VERTICAL;
    // TODO : add a config list for that
    private static Set<Material> cutset = new HashSet<>() {{
        addAll(MaterialSetTag.LOGS.getValues());
    }};
    
    protected void setCuttingLimit(int limit){
        this.limit = limit;
    }
    
    protected void setExtendedMode(CuttingMode mode){
        this.mode = mode;
    }
    
    public static void cutTreeFromBlock(Block origin, int limit, CuttingMode mode, @Nullable ItemStack tool){
        if(mode.equals(CuttingMode.VERTICAL) || mode.equals(CuttingMode.VERTICAL_DOUBLE)){
            for(int i = 1; i < limit; i++){
                Block b = origin.getRelative(0,i,0);
                if(b.getType().equals(origin.getType()) && cutset.contains(b.getType())){
                    b.breakNaturally(tool);
                }
                if(mode.equals(CuttingMode.VERTICAL_DOUBLE)){
                    Block b2 = origin.getRelative(0,-i,0);
                    if(b2.getType().equals(origin.getType()) && cutset.contains(b2.getType())){
                        b2.breakNaturally(tool);
                    }
                }
            }
        } else if(mode.equals(CuttingMode.EXTENDED)){
            Set<Block> scanqueue = new HashSet<>();
            List<Block> destroylist = new ArrayList<>();
            scanqueue.add(origin);
            int iterations = 0;
            while(scanqueue.size() >= 1 && iterations <= limit){
                iterations++;
                Set<Block> future = new HashSet<>();
                for(Block b1 : scanqueue){
                    for(Block b2 : getNeighbours(b1)){
                        if(!destroylist.contains(b2) && b2.getType().equals(origin.getType()) && cutset.contains(b2.getType())){
                            destroylist.add(b2);
                            future.add(b2);
                        }
                    }
                }
                scanqueue.clear();
                scanqueue.addAll(future);
            }
    
            
            
            new BukkitRunnable(){
                int it = 0;
                final List<Block> reversed = Lists.reverse(destroylist);
                final int limit = reversed.size();
                @Override
                public void run(){
                    if(it >= limit){
                        this.cancel();
                        return;
                    }
                    Block condamned = reversed.get(it);
                    Location mid = condamned.getLocation().add(.5,.5,.5);
                    Sound s = condamned.getBlockSoundGroup().getBreakSound();
                    BlockData d = condamned.getBlockData();
                    condamned.breakNaturally(tool);
                    // must absolutely get "s" and "d" in variables BEFORE condamned.breakNaturally();
                    //
                    // concerning the animation I must find a way to make it consistent through all 3 cutting modes (VERTICAL, VERTICAL_DOUBLE, EXTENDED)
                    condamned.getWorld().spawnParticle(Particle.BLOCK_DUST,mid,16,.5,.5,.5,0,d);
                    condamned.getWorld().playSound(mid,s,1,1);
                    
                    it++;
                }
            }.runTaskTimer(Main.getPlugin(),5,0);
        }
    }
    
    public static Set<Block> getNeighbours(Block origin){
        Set<Block> scanqueue = new HashSet<>();
        for(int x = -1; x <=1; x++){
            for(int y = -1; y <=1; y++){
                for(int z = -1; z <=1; z++){
                    Block b = origin.getRelative(x,y,z);
                    if(b.getType().equals(origin.getType()) && b.isPreferredTool(new ItemStack(Material.IRON_AXE))){
                        scanqueue.add(b);
                    }
                }
            }
        }
        return scanqueue;
    }
}
