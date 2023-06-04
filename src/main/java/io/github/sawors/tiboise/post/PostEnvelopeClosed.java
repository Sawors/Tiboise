package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.items.ItemSerializer;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class PostEnvelopeClosed extends SendableItem implements Listener {
    
    public PostEnvelopeClosed(){
        setMaterial(Material.PAPER);
        setDisplayName(Component.text("Post Envelope (Closed)").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    }
    
    @EventHandler
    public static void openEnvelope(PlayerInteractEvent event){
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        final Player p = event.getPlayer();
        if(event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND) && TiboiseItem.getItemId(item).equals(new PostEnvelopeClosed().getId())){
            // open the envelope
            final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            final String contentText = container.get(PostEnvelope.getContentTextKey(), PersistentDataType.STRING);
            final String contentItem = container.get(PostEnvelope.getContentItemKey(), PersistentDataType.STRING);
            
            // if it contains an item, drop it at the player's location
            if(contentItem != null && contentItem.length() >= 1){
                ItemStack extractedItem = new ItemSerializer().deserializeSingleItem(contentItem);
                p.getWorld().dropItem(p.getLocation(),extractedItem);
            }
            
            // sending the contained message
            final OfflinePlayer sender = getSender(item);
            Component message = Component.text(sender.getName()+"A letter from "+sender.getName()+" :\n").color(NamedTextColor.GOLD)
            .append(Component.text(contentText != null ? contentText : "This letter is empty...").color(NamedTextColor.GRAY));
            p.sendMessage(message);
            
            // removing the envelope and replacing it with an opened variant
            final int baseAmount = item.getAmount();
            PostEnvelope envelope = new PostEnvelope();
            envelope.setVariant(TiboiseItem.getItemVariant(item));
            final ItemStack emptyEnvelope = envelope.get();
            if(baseAmount > 1){
                item.setAmount(item.getAmount()-1);
                p.getInventory().addItem(emptyEnvelope);
            } else {
                p.getInventory().setItemInMainHand(emptyEnvelope);
            }
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        
        PostEnvelopeClosed closedEnvelope = new PostEnvelopeClosed();
        closedEnvelope.setVariant(this.getVariant());
        
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),closedEnvelope.get());
        recipe.addIngredient(new PostEnvelope().get());
        recipe.addIngredient(new PostStamp().get());
        return recipe;
    }
}
