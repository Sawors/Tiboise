package io.github.sawors.tiboise.items;

import io.github.sawors.tiboise.core.ItemTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Fence;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TiboiseWrench extends TiboiseItem implements Listener {
    public TiboiseWrench(){
        setMaterial(Material.STICK);
        setId("wrench");
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setDisplayName(Component.text("Wrench").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setLore(List.of(
                Component.text(ChatColor.GRAY+"This wrench is able to modify block states,"),
                Component.text(ChatColor.GRAY+"Usually this consist in a rotation")
        ));
    }

    @EventHandler
    public void onPlayerUseWrench(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        boolean sneaking = event.getPlayer().isSneaking();
        if(b != null && Objects.equals(event.getHand(), EquipmentSlot.HAND)){
            
            BlockData data = b.getBlockData();

            // TOTEST
            //  Check if the rotation works and if this blockstate modification is enough to actually modify the block
            if(data instanceof Orientable or){
                // rotate along X/Y/Z axes if not sneaking
                Axis axis = or.getAxis();
                if(!sneaking){
                    if(axis.equals(Axis.X)){
                        axis = Axis.Z;
                    } else if(axis.equals(Axis.Z)){
                        axis = Axis.X;
                    }
                }
                // invert rotation axis if sneaked (X/Z -> Y, Y -> X)
                else {
                    if((axis.equals(Axis.X) || axis.equals(Axis.Z)) && or.getAxes().contains(Axis.Y)) {
                        axis = Axis.Y;
                    } else if(or.getAxes().contains(Axis.X)){
                        axis = Axis.X;
                    }
                }
                or.setAxis(axis);
            } else if(data instanceof Fence fc){
                // if already connected : disconnect
                if(fc.getFaces().size() >= 1){
                    for(BlockFace face : fc.getFaces()){
                        fc.setFace(face, false);
                    }
                }
                // if disconnected : try to connect
                else {
                    for(BlockFace face : fc.getAllowedFaces()){
                        Block relative = b.getRelative(face);
                        if(relative.getBlockData() instanceof Fence f2){
                            BlockData b2dt = f2.clone();
                            fc.setFace(face,true);
                            if(sneaking){
                                relative.setBlockData(b2dt);
                            }
                        }
                    }
                }
            }
    
            b.setBlockData(data);
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        return new ShapedRecipe(getIdAsKey(),this.get()).shape("XIX","XII","IXX").setIngredient('X',Material.AIR).setIngredient('I',Material.IRON_INGOT);
    }
}
