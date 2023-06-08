package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostEnvelopeClosed extends SendableItem implements Listener {
    
    protected static Map<UUID, Component> lastLetters = new HashMap<>();
    
    //private final static Component emptyItemMessage = Component.text("Drag and drop item to add (1 max)").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE);
    
    private String content;
    private String author;
    
    public PostEnvelopeClosed(){
        setMaterial(Material.PAPER);
        setDisplayName(Component.text("Letter").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        setContent("");
        setAuthor("?????");
    }
    
    @Override
    public ItemStack get() {
        addData(getContentTextKey(),content);
        addData(getAuthorKey(),author);
        return super.get();
    }
    
    @EventHandler
    public static void openEnvelope(PlayerInteractEvent event){
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        final Player p = event.getPlayer();
        if(event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND) && getItemId(item).equals(getId(PostEnvelopeClosed.class)) && event.getAction().isRightClick()){
            event.setCancelled(true);
            // open the envelope
            final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            final String contentText = container.get(PostEnvelopeClosed.getContentTextKey(), PersistentDataType.STRING);
            //final String contentItem = container.get(PostEnvelopeClosed.getContentItemKey(), PersistentDataType.STRING);
            
//            if(contentItem != null && contentItem.length() >= 1){
//                ItemStack extractedItem = new ItemSerializer().deserializeSingleItem(contentItem);
//                p.getWorld().dropItem(p.getLocation(),extractedItem);
//            }
            
            // sending the contained message
            final String sender = getAuthor(item);
            Component message = Component.text("A letter from "+sender+" :\n")
                    .color(NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.suggestCommand("/letter"))
                    .append(Component.text(contentText != null ? " "+contentText : "This letter is empty...").color(NamedTextColor.GRAY));
            p.sendMessage(message);
            lastLetters.put(p.getUniqueId(),message);
            
            // removing the envelope and replacing it with an opened variant
            final int baseAmount = item.getAmount();
            PostEnvelope envelope = new PostEnvelope();
            envelope.setVariant(getItemVariant(item));
            final ItemStack emptyEnvelope = envelope.get();
            if(baseAmount > 1){
                item.setAmount(item.getAmount()-1);
                p.getInventory().addItem(emptyEnvelope);
            } else {
                p.getInventory().setItemInMainHand(emptyEnvelope);
            }
        }
    }
    
    @EventHandler
    public static void transferData(PlayerInteractEvent event){
        final Player p = event.getPlayer();
        final Block clickedBlock = event.getClickedBlock();
        final ItemStack item = p.getInventory().getItemInMainHand();
        if(clickedBlock != null && event.getAction().isRightClick() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)
            && clickedBlock.getState() instanceof Lectern lectern
            && getItemId(item).equals(getId(PostEnvelope.class))
        ) {
            final Inventory inv = lectern.getSnapshotInventory();
            if(inv instanceof LecternInventory lectInv && lectInv.getBook() != null){
                event.setCancelled(true);
                final BookMeta meta = (BookMeta) lectInv.getBook().getItemMeta();
                Component content = Component.text("");
                for(int i = 1; i<=meta.getPageCount(); i++){
                    content = content.append(meta.page(i).append(Component.text(" ")));
                }
                String text = TiboiseUtils.extractContent(content);
                String preview;
                if(text.contains("\n")){
                     preview = text.substring(0,Math.min(text.indexOf("\n"),24))+"...";
                } else {
                     preview = text.substring(0,Math.min(text.length(),24))+"...";
                }
                
                preview = preview.replaceAll("\n","");
                text = text.replaceAll("\n","\n ");
                PostEnvelopeClosed envelope = new PostEnvelopeClosed();
                final String variant = getItemVariant(item);
                final String author = p.getName();
                if(variant.length() > 0) envelope.setVariant(variant);
                envelope.setContent(text);
                envelope.setAuthor(author);
                envelope.addLore(List.of(Component.text("From "+author+" :").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),Component.text(preview).color(NamedTextColor.GRAY)));
                
                item.setAmount(item.getAmount()-1);
                // putting it in a runnable since there is a bug where the event is triggered twice
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(p.getInventory().getItemInMainHand().getType().isAir()){
                            p.getInventory().setItemInMainHand(envelope.get());
                        } else {
                            for(Map.Entry<Integer,ItemStack> overflow : p.getInventory().addItem(envelope.get()).entrySet()){
                                p.getWorld().dropItem(p.getLocation(),overflow.getValue());
                            }
                        }
                    }
                }.runTask(Tiboise.getPlugin());
            }
        }
    }
    
    public static Component getLastLetter(UUID player){
        return lastLetters.getOrDefault(player,Component.text("You have no recent letter.").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
    }
    
    /*@Override
    public @Nullable Recipe getRecipe() {
        
        PostEnvelopeClosed closedEnvelope = new PostEnvelopeClosed();
        closedEnvelope.setVariant(this.getVariant());
        
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),closedEnvelope.get());
        recipe.addIngredient(new PostEnvelope().get());
        recipe.addIngredient(new PostStamp().getPlaceHolder());
        recipe.addIngredient(new RecipeChoice.ExactChoice(new ItemStack(Material.WRITABLE_BOOK),new ItemStack(Material.WRITTEN_BOOK)));
        return recipe;
    }
    
    @EventHandler
    public static void transferDataToEnvelope(PrepareItemCraftEvent event){
        if(event.getRecipe() == null) return;
        try{
            final CraftingInventory inv = event.getInventory();
            ItemStack[] grid = inv.getMatrix();
            
            
            ItemStack envelope = Arrays.stream(grid).filter(c -> Objects.equals(TiboiseItem.getItemId(c),getId(PostEnvelope.class))).findFirst().orElse(null);
            ItemStack stamp = Arrays.stream(grid).filter(c -> Objects.equals(TiboiseItem.getItemId(c),getId(PostStamp.class))).findFirst().orElse(null);
            ItemStack book = Arrays.stream(grid).filter(c -> c!=null && (c.getType().equals(Material.WRITABLE_BOOK) || c.getType().equals(Material.WRITTEN_BOOK))).findFirst().orElse(null);
            
            if(
                    stamp == null || envelope == null || book == null
                    || book.getAmount() != 1
            ) return;
            
            
            PostEnvelopeClosed resultEnvelope = new PostEnvelopeClosed();
            resultEnvelope.setVariant(TiboiseItem.getItemVariant(envelope));
            
            OfflinePlayer receiver = PostStamp.getReceiver(stamp);
            OfflinePlayer sender = PostStamp.getSender(stamp);
            
            if(sender != null && sender.getName() != null && receiver != null && receiver.getName() != null){
                
                BookMeta bookMeta = (BookMeta) book.getItemMeta().clone();
                StringBuilder content = new StringBuilder();
                for(Component text : bookMeta.pages()){
                    content.append(((TextComponent) text).content()).append(" ");
                }
                
                resultEnvelope.addData(getContentTextKey(),content.toString());
                //resultEnvelope.addData(getContentItemKey(),"");
                resultEnvelope.setLore(List.of(
                        Component.text(" From ").color(NamedTextColor.GOLD)
                                .append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))
                                .append(Component.text(" to ").color(NamedTextColor.GOLD))
                                .append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                ));
                
                ItemStack finalOutput = resultEnvelope.get();
                PostStamp.setSenderAndReceiver(finalOutput,sender.getUniqueId(),receiver.getUniqueId());
                ItemMeta meta = finalOutput.getItemMeta();
                meta.displayName(Component.text("Letter").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                finalOutput.setItemMeta(meta);
                inv.setResult(finalOutput);
            }
            
            
            
            //final String UUID
            
        } catch (NoSuchElementException e){
            e.printStackTrace();
        }
    }
    
    // do the post-crafting cleanup manually since bukkit does not correctly do it
    @EventHandler(priority = EventPriority.HIGH)
    public static void onPlayerCraftBook(InventoryClickEvent event){
        if(event.getClickedInventory() instanceof CraftingInventory inv
                && event.getSlotType().equals(InventoryType.SlotType.RESULT)
                && event.isLeftClick()
                && event.getCurrentItem() != null
                && Objects.equals(TiboiseItem.getItemId(event.getCurrentItem()), getId(PostEnvelopeClosed.class))
                && Arrays.stream(inv.getMatrix()).anyMatch(i -> i!= null && (i.getType().equals(Material.WRITABLE_BOOK) || i.getType().equals(Material.WRITTEN_BOOK)))
                && Arrays.stream(inv.getMatrix()).anyMatch(i ->  Objects.equals(TiboiseItem.getItemId(i),getId(PostStamp.class)))
                && Arrays.stream(inv.getMatrix()).anyMatch(i ->  Objects.equals(TiboiseItem.getItemId(i),getId(PostEnvelope.class)))
                && (Arrays.stream(inv.getMatrix()).filter(Objects::isNull).count() == 9-3 || Arrays.stream(inv.getMatrix()).filter(Objects::isNull).count() == 4-3)
        ) {
            ItemStack[] grid = inv.getMatrix();
            
            Map<Integer,Integer> nextAmounts = new HashMap<>();
            
            int slot = 0;
            for (final ItemStack item : grid) {
                slot++;
                if (item == null) continue;
                final int amountLowered = item.getAmount()-1;
                if(amountLowered > 0){
                    nextAmounts.put(slot,amountLowered);
                }
                if (item.getType().equals(Material.WRITABLE_BOOK) || item.getType().equals(Material.WRITTEN_BOOK)) {
                    
                    final ItemStack book = item.clone().asOne();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            final HumanEntity player = event.getWhoClicked();
                            Map<Integer, ItemStack> overflow = player.getInventory().addItem(book);
                            if(overflow.size() == 0){
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP,1,1);
                            }
                            for(Map.Entry<Integer,ItemStack> overflowItem : overflow.entrySet()){
                                player.getWorld().dropItem(player.getLocation(),overflowItem.getValue());
                            }
                            
                        }
                    }.runTask(Tiboise.getPlugin());
                    break;
                }
            }
            
            
            new BukkitRunnable(){
                @Override
                public void run() {
                    for(Map.Entry<Integer,Integer> entry : nextAmounts.entrySet()){
                        try{
                            inv.setItem(entry.getKey(), Objects.requireNonNull(inv.getItem(entry.getKey())).asQuantity(entry.getValue()));
                        } catch (NullPointerException uwu){
                            uwu.printStackTrace();
                        }
                    }
                }
            }.runTaskLater(Tiboise.getPlugin(),1);
        }
    }*/
    
    /*@EventHandler
    public static void addItemToEnvelope(InventoryClickEvent event){
        ItemStack envelope = event.getCurrentItem();
        ItemStack item = event.getCursor();
        final Inventory inv = event.getClickedInventory();
        if(item != null && inv != null && event.getSlotType().equals(InventoryType.SlotType.CONTAINER) && envelope != null && TiboiseItem.getItemId(envelope).equals(new PostEnvelopeClosed().getId()) && !TiboiseItem.getItemId(item).equals(new PostEnvelopeClosed().getId()) && !TiboiseItem.getItemId(item).equals(PackingScotch.packedBlockItemId)){
            ItemMeta meta = envelope.getItemMeta();
            final String content = meta.getPersistentDataContainer().get(getContentItemKey(),PersistentDataType.STRING);
            
            if(!item.getType().isAir() && (content == null || content.length() == 0) && event.getClick().isLeftClick()){
                // add item
                event.setCancelled(true);
                final String data = new ItemSerializer().serialize(item.asOne(),0);
                final String itemMaterialName = TiboiseUtils.capitalizeFirstLetter(item.getType().toString()).replaceAll("_"," ");
                meta.getPersistentDataContainer().set(getContentItemKey(),PersistentDataType.STRING,data);
                item.setAmount(item.getAmount()-1);
                List<Component> lore = meta.lore();
                if(lore != null){
                    lore.subList(0,lore.size()-2);
                    lore.add(Component.text("With item : "+itemMaterialName).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                meta.lore(lore);
            } else if(item.getType().isAir() && event.getClick().isRightClick()){
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
                            lore.subList(0,lore.size()-2);
                            lore.add(emptyItemMessage);
                        }
                        meta.lore(lore);
                    }
                }
            }
            envelope.setItemMeta(meta);
        }
    }*/
    
    public static NamespacedKey getContentTextKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-text");
    }
    
    public static NamespacedKey getAuthorKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-content-author");
    }
    
    public void setContent(String content){
        this.content = content;
    }
    
    public static String getContent(ItemStack itemStack){
        if(itemStack != null && itemStack.hasItemMeta()){
            final String content = itemStack.getItemMeta().getPersistentDataContainer().get(getContentTextKey(),PersistentDataType.STRING);
            return content != null ? content : "";
        }
        return "";
    }
    
    public static String getAuthor(ItemStack itemStack){
        if(itemStack != null && itemStack.hasItemMeta()){
            final String content = itemStack.getItemMeta().getPersistentDataContainer().get(getAuthorKey(),PersistentDataType.STRING);
            return content != null ? content : "";
        }
        return "";
    }
    
    public void setAuthor(String author){
        this.author = author;
    }
//    public static NamespacedKey getContentItemKey(){
//        return new NamespacedKey(Tiboise.getPlugin(),"post-content-item");
//    }
}
