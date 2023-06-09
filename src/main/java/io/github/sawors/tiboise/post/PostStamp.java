package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PostStamp extends TiboiseItem implements Listener {
    
    private PostLetterBox destination;
    
    public PostStamp(){
        setMaterial(Material.PAPER);
        setVariant(formatTextToId(StampVariants.DEFAULT.toString()));
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setHelpText("");
    }
    
    public PostStamp(StampVariants variant){
        this();
        this.setVariant(formatTextToId(variant.toString()));
    }
    
    protected ItemStack getPlaceHolder(){
        PostStamp stamp = new PostStamp();
        stamp.setLore(buildLore("Your Friend","Their House"));
        ItemStack out = stamp.get();
        ItemMeta meta = out.getItemMeta();
        meta.displayName(Objects.requireNonNull(meta.displayName()).color(NamedTextColor.DARK_PURPLE));
        out.setItemMeta(meta);
        return out;
    }
    
    public enum StampVariants {
        DEFAULT
    }
    
    public PostLetterBox getDestination() {
        return destination;
    }
    
    public void setDestination(PostLetterBox destination) {
        this.destination = destination;
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),new PostStamp().get());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.HONEYCOMB);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
    
    @Override
    public ItemStack get() {
        ItemStack base = super.get();
        ItemMeta meta = base.getItemMeta();
        if(getDestination() != null){
            meta.getPersistentDataContainer().set(getDestinationKey(),PersistentDataType.STRING,getDestination().toString());
        }
        base.setItemMeta(meta);
        return base;
    }
    
    public static @Nullable PostLetterBox getDestination(ItemStack item){
        if(item != null && item.hasItemMeta()){
            final String from = item.getItemMeta().getPersistentDataContainer().get(getDestinationKey(),PersistentDataType.STRING);
            if(from != null){
                return PostLetterBox.deserialize(from);
            }
        }
        return null;
    }
    
    public static void transferDestination(ItemStack stamp, ItemStack to){
        if(stamp != null && stamp.hasItemMeta() && to != null && !to.getType().isAir()){
            final String from = stamp.getItemMeta().getPersistentDataContainer().get(getDestinationKey(),PersistentDataType.STRING);
            if(from != null){
                ItemMeta meta = to.getItemMeta();
                meta.getPersistentDataContainer().set(getDestinationKey(),PersistentDataType.STRING,from);
                to.setItemMeta(meta);
            }
        }
    }
    
    public static NamespacedKey getDestinationKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-destination");
    }
    
    @EventHandler
    public static void createStampDestination(PlayerInteractEvent event){
        final Block b = event.getClickedBlock();
        final Player p = event.getPlayer();
        final ItemStack stampItem = p.getInventory().getItemInMainHand();
        if(
                b != null
                && event.getAction().isRightClick()
                && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)
                && getItemId(stampItem).equals(getId(PostStamp.class))
                && b.getState() instanceof Sign sign
                && sign.lines().size() >= 3
                && ((TextComponent)sign.line(0)).content().equals(PostLetterBox.getLetterBoxIdentifier())
                && sign.getPersistentDataContainer().get(PostLetterBox.getOwnerKey(), PersistentDataType.STRING) != null
        ){
               try{
                   PostLetterBox clicked = new PostLetterBox(UUID.fromString(Objects.requireNonNull(sign.getPersistentDataContainer().get(PostLetterBox.getOwnerKey(), PersistentDataType.STRING))),sign.getBlock());
                   
                   PostStamp editedStamp = new PostStamp();
                   editedStamp.setVariant(getItemVariant(stampItem));
                   editedStamp.setDestination(clicked);
                   editedStamp.setLore(buildLore(((TextComponent)sign.line(1)).content(),((TextComponent)sign.line(2)).content()));
                   editedStamp.setDisplayName(Component.text("Post Stamp").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                   
                   stampItem.setAmount(stampItem.getAmount()-1);
                   
                   p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,.75f,1.25f);
                   
                   if(p.getInventory().getItemInMainHand().getType().isAir()){
                       p.getInventory().setItemInMainHand(editedStamp.get());
                   } else {
                       for(ItemStack overflow : p.getInventory().addItem(editedStamp.get()).values()){
                           p.getWorld().dropItem(p.getLocation(),overflow);
                       }
                   }
               } catch (IllegalArgumentException e){
                   e.printStackTrace();
               }
        }
    }
    
    
//    public static OfflinePlayer getSender(ItemStack sendableItem){
//        try{
//            Pair<UUID,UUID> couple = deserializeSenderAndReceiver(Objects.requireNonNull(sendableItem.getItemMeta().getPersistentDataContainer().get(getDestinationKey(), PersistentDataType.STRING)));
//            UUID sender = couple.getKey();
//            return Bukkit.getOfflinePlayer(sender);
//        } catch (NullPointerException n){
//            return null;
//        }
//    }
//
//    public static OfflinePlayer getReceiver(ItemStack sendableItem){
//        try{
//            Pair<UUID,UUID> couple = deserializeSenderAndReceiver(Objects.requireNonNull(sendableItem.getItemMeta().getPersistentDataContainer().get(getDestinationKey(), PersistentDataType.STRING)));
//            UUID sender = couple.getValue();
//            return Bukkit.getOfflinePlayer(sender);
//        } catch (NullPointerException n){
//            return null;
//        }
//    }
//
//    public static Pair<UUID,UUID> deserializeSenderAndReceiver(ItemStack source) throws ArrayIndexOutOfBoundsException, IllegalStateException{
//        final String serializedData = source.getItemMeta().getPersistentDataContainer().get(getDestinationKey(),PersistentDataType.STRING);
//        if(serializedData == null) return null;
//        if(!serializedData.contains(":")){
//            throw new IllegalStateException("this string does not contain any stamp data");
//        }
//        final String senderString = serializedData.substring(0,serializedData.indexOf(":"));
//        final String receiverString = serializedData.substring(serializedData.indexOf(":")+1);
//        final UUID sender = UUID.fromString(senderString);
//        final UUID receiver = UUID.fromString(receiverString);
//        return Pair.of(sender,receiver);
//    }
//
//    private static Pair<UUID,UUID> deserializeSenderAndReceiver(String serializedData) throws ArrayIndexOutOfBoundsException, IllegalStateException{
//        if(!serializedData.contains(":")){
//            throw new IllegalStateException("this string does not contain any stamp data");
//        }
//        final String senderString = serializedData.substring(0,serializedData.indexOf(":"));
//        final String receiverString = serializedData.substring(serializedData.indexOf(":")+1);
//        final UUID sender = UUID.fromString(senderString);
//        final UUID receiver = UUID.fromString(receiverString);
//        return Pair.of(sender,receiver);
//    }
//
//    public static void setSenderAndReceiver(ItemStack item, UUID sender, UUID receiver){
//        if(item != null){
//            ItemMeta meta = item.getItemMeta();
//            meta.getPersistentDataContainer().set(getDestinationKey(),PersistentDataType.STRING,generatePathString(sender,receiver));
//            item.setItemMeta(meta);
//        }
//    }
//
//    @EventHandler
//    public static void anvilPathingCreate(PrepareAnvilEvent event){
//        AnvilInventory inv = event.getInventory();
//        ItemStack input = (inv.getFirstItem() != null && !inv.getFirstItem().getType().isAir()) ? inv.getFirstItem() : inv.getSecondItem();
//        ItemStack result = inv.getResult();
//        if(input == null || input.getType().isAir() || !TiboiseItem.getItemId(input).equals(getId(PostStamp.class))){
//            return;
//        }
//
//        final String userInput = inv.getRenameText();
//        Pair<String,String> parsed = parsePathInput(userInput);
//        if(parsed != null){
//            final String senderName = PlayerDataManager.getRealName(parsed.getKey());
//            final String receiverName = PlayerDataManager.getRealName(parsed.getValue());
//            if(senderName != null && receiverName != null && result != null){
//                result = result.clone();
//                OfflinePlayer sender = Bukkit.getOfflinePlayerIfCached(senderName);
//                OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(receiverName);
//                if(sender == null || receiver == null) return;
//
//                ItemMeta meta = result.getItemMeta();
//                meta.displayName(Component.text("Stamp").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
//                meta.lore(buildLore(senderName,receiverName));
//                PersistentDataContainer container = meta.getPersistentDataContainer();
//                container.set(getDestinationKey(),PersistentDataType.STRING,generatePathString(sender.getUniqueId(),receiver.getUniqueId()));
//                final ItemStack finalResult = result;
//                new BukkitRunnable(){
//                    @Override
//                    public void run() {
//                        finalResult.setItemMeta(meta);
//                        inv.setResult(finalResult);
//                    }
//                }.runTask(Tiboise.getPlugin());
//
//            }
//
//        }
//
//    }
//
//    private static Pair<String, String> parsePathInput(String input){
//        final String cleanInput = input.replaceAll(" ","");
//        String identifier = cleanInput.contains("->") ? "->" : cleanInput.contains(":") ? ":" : null;
//        if(identifier == null || cleanInput.indexOf(identifier) == 0 || cleanInput.indexOf(identifier) == cleanInput.length()-identifier.length()) return null;
//        return Pair.of(cleanInput.substring(0,cleanInput.indexOf(identifier)),cleanInput.substring(cleanInput.indexOf(identifier)+identifier.length()));
//    }
//
//    private static String generatePathString(UUID sender, UUID receiver){
//        return sender.toString()+":"+receiver;
//    }
    
    public static List<Component> buildLore(String recipientName, String houseName){
        return List.of(
                Component.text("> To ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(recipientName).color(NamedTextColor.GOLD)).append(
                        Component.text(" at ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(houseName).color(NamedTextColor.GOLD))
                )
                
        );
    }
}
