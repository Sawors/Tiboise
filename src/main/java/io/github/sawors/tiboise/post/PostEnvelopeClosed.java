package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.items.ItemSerializer;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class PostEnvelopeClosed extends SendableItem implements Listener {
    
    private final static Component emptyItemMessage = Component.text("Click with a single item on this envelope to put it inside (1 item max)").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE);
    
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
            final String contentText = container.get(PostEnvelopeClosed.getContentTextKey(), PersistentDataType.STRING);
            final String contentItem = container.get(PostEnvelopeClosed.getContentItemKey(), PersistentDataType.STRING);
            
            // if it contains an item, drop it at the player's location
            if(contentItem != null && contentItem.length() >= 1){
                ItemStack extractedItem = new ItemSerializer().deserializeSingleItem(contentItem);
                p.getWorld().dropItem(p.getLocation(),extractedItem);
            }
            
            // sending the contained message
            final OfflinePlayer sender = PostStamp.getSender(item);
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
        recipe.addIngredient(new PostStamp().getPlaceHolder());
        recipe.addIngredient(Material.WRITTEN_BOOK);
        return recipe;
    }
    
    @EventHandler
    public static void transferDataToEnvelope(PrepareItemCraftEvent event){
        try{
            final CraftingInventory inv = event.getInventory();
            ItemStack output = inv.getResult();
            ItemStack[] grid = inv.getMatrix();
            Stream<ItemStack> stream = Arrays.stream(grid);
            ItemStack envelope = Arrays.stream(grid).filter(c -> Objects.equals(TiboiseItem.getItemId(c),new PostEnvelope().getId())).findFirst().orElse(null);
            ItemStack stamp = Arrays.stream(grid).filter(c -> Objects.equals(TiboiseItem.getItemId(c),new PostStamp().getId())).findFirst().orElse(null);
            if(stamp == null || envelope == null) return;
            PostEnvelopeClosed resultEnvelope = new PostEnvelopeClosed();
            resultEnvelope.setVariant(TiboiseItem.getItemVariant(envelope));
            resultEnvelope.setDisplayName(envelope.getItemMeta().displayName());
            
            OfflinePlayer receiver = PostStamp.getReceiver(stamp);
            OfflinePlayer sender = PostStamp.getSender(stamp);
            
            ItemStack book = null;
            Integer bookIndex = null;
            for(int i = 0; i<grid.length; i++){
                final ItemStack item = grid[i];
                if(item.getType().equals(Material.WRITTEN_BOOK)){
                    book = item;
                    bookIndex = i;
                }
            }
            if(sender.getName() != null && receiver.getName() != null){
                if(book != null){
                    BookMeta bookMeta = (BookMeta) book.getItemMeta();
                    final String title = bookMeta.getTitle();
                    StringBuilder content = new StringBuilder();
                    for(Component text : bookMeta.pages()){
                        content.append(((TextComponent) text).content()).append(" ");
                    }
                    
                    resultEnvelope.addData(getContentTitleKey(),title);
                    resultEnvelope.addData(getContentTextKey(),content.toString());
                    resultEnvelope.addData(getContentItemKey(),"");
                    resultEnvelope.setLore(List.of(
                            Component.text("From ").color(NamedTextColor.GOLD)
                                    .append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))
                                    .append(Component.text(" to ").color(NamedTextColor.GOLD))
                                    .append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.text(title != null ? title : "").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.text(""),
                            Component.text("Click with a single item on this envelope to put it inside (1 item max)").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE)
                    ));
                    
                } else {
                    resultEnvelope.setLore(List.of(
                            Component.text("From ").color(NamedTextColor.GOLD)
                                    .append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))
                                    .append(Component.text(" to ").color(NamedTextColor.GOLD))
                                    .append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.text(""),
                            Component.text("Click with a single item on this envelope to put it inside (1 item max)").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE)
                    ));
                }
                
                inv.setResult(resultEnvelope.get());
            }
            
            //final String UUID
            
        } catch (NoSuchElementException e){
            e.printStackTrace();
        }
    }
    
    @EventHandler
    public static void addItemToEnvelope(InventoryClickEvent event){
        ItemStack envelope = event.getCurrentItem();
        ItemStack item = event.getCursor();
        final Inventory inv = event.getClickedInventory();
        if(inv != null && envelope != null && TiboiseItem.getItemId(envelope).equals(new PostEnvelopeClosed().getId())){
            ItemMeta meta = envelope.getItemMeta();
            final String content = meta.getPersistentDataContainer().get(getContentItemKey(),PersistentDataType.STRING);
            
            if(item != null && (content == null || content.length() == 0) && event.getClick().isLeftClick()){
                // add item
                event.setCancelled(true);
                final String data = new ItemSerializer().serialize(item.asOne(),0);
                final String itemMaterialName = TiboiseUtils.capitalizeFirstLetter(item.getType().toString()).replaceAll("_"," ");
                meta.getPersistentDataContainer().set(getContentItemKey(),PersistentDataType.STRING,data);
                item.setAmount(item.getAmount()-1);
                List<Component> lore = meta.lore();
                if(lore != null){
                    lore.subList(0,lore.size()-1);
                    lore.add(Component.text("With item : "+itemMaterialName).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                meta.lore(lore);
            } else if(item == null && event.getClick().isRightClick()){
                // retrieve item
                final String data = meta.getPersistentDataContainer().get(getContentItemKey(),PersistentDataType.STRING);
                if(data != null && data.length() > 1){
                    event.setCancelled(true);
                    ItemStack extracted = new ItemSerializer().deserializeSingleItem(data);
                    if(extracted != null){
                        Map<Integer,ItemStack> overflow = inv.addItem(extracted);
                        meta.getPersistentDataContainer().set(getContentItemKey(),PersistentDataType.STRING,"");
                        if(overflow.size() > 0){
                            for(Map.Entry<Integer,ItemStack> overflowItem : overflow.entrySet()){
                                event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(),overflowItem.getValue());
                            }
                        }
                        
                        List<Component> lore = meta.lore();
                        if(lore != null){
                            lore.subList(0,lore.size()-1);
                            lore.add(emptyItemMessage);
                        }
                        meta.lore(lore);
                    }
                }
            }
            envelope.setItemMeta(meta);
        }
    }
    
    public static NamespacedKey getContentTextKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-text");
    }
    
    public static NamespacedKey getContentItemKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-item");
    }
    
    public static NamespacedKey getContentTitleKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-title");
    }
}
