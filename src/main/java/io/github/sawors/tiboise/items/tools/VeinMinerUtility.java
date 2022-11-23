package io.github.sawors.tiboise.items.tools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinMinerUtility {
    private Block origin;
    private int limit = 32;
    
    public VeinMinerUtility(Block origin){
        this.origin = origin;
    }
    
    public VeinMinerUtility setLimit(int limit){
        this.limit = limit;
        return this;
    }
    
    public List<Block> getVein(Set<Material> filter){
        List<Block> output = new ArrayList<>();
        Set<Block> scanqueue = new HashSet<>();
        scanqueue.add(origin);
        int iterations = 0;
        while(scanqueue.size() >= 1 && iterations <= limit){
            iterations++;
            Set<Block> future = new HashSet<>();
            for(Block b1 : scanqueue){
                for(Block b2 : new VeinMinerUtility(b1).getNeighbours()){
                    if(!output.contains(b2) && b2.getType().equals(origin.getType()) && filter.contains(b2.getType())){
                        output.add(b2);
                        future.add(b2);
                    }
                }
            }
            scanqueue.clear();
            scanqueue.addAll(future);
        }
        return output;
    }
    
    public List<Block> getVerticalVein(boolean bidirectional, Set<Material> filter){
        List<Block> output = new ArrayList<>();
        output.add(origin);
        for(int i = 1; i < limit; i++){
            Block b = origin.getRelative(0,i,0);
            if(b.getType().equals(origin.getType()) && filter.contains(b.getType()) && !output.contains(b)){
                output.add(b);
            }
            if(bidirectional){
                Block b2 = origin.getRelative(0,-i,0);
                if(b2.getType().equals(origin.getType()) && filter.contains(b2.getType()) && !output.contains(b2)){
                    output.add(b2);
                }
            }
        }
        return output;
    }
    
    public List<Block> getNeighbours(){
        List<Block> scanqueue = new ArrayList<>();
        scanqueue.add(origin);
        for(int x = -1; x <=1; x++){
            for(int y = -1; y <=1; y++){
                for(int z = -1; z <=1; z++){
                    Block b = origin.getRelative(x,y,z);
                    if(b.getType().equals(origin.getType()) && b.isPreferredTool(new ItemStack(Material.IRON_AXE)) && !scanqueue.contains(b)){
                        scanqueue.add(b);
                    }
                }
            }
        }
        return scanqueue;
    }
}
