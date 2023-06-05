package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.PlayerDataManager;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PostStamp extends TiboiseItem implements Listener {
    
    UUID sender;
    UUID receiver;
    
    public PostStamp(){
        setMaterial(Material.PAPER);
        setVariant(formatTextToId(StampVariants.DEFAULT.toString()));
        setHelpText("To fill this stamp, put it in an anvil and rename it to [sender]->[receiver] (replace [sender] by your name and [receiver] by the name of the person you want to send the letter to)");
    }
    
    public PostStamp(StampVariants variant){
        this();
        this.setVariant(formatTextToId(variant.toString()));
    }
    
    protected ItemStack getPlaceHolder(){
        PostStamp stamp = new PostStamp();
        stamp.setLore(buildLore("?????","?????"));
        ItemStack out = stamp.get();
        ItemMeta meta = out.getItemMeta();
        meta.displayName(Objects.requireNonNull(meta.displayName()).color(NamedTextColor.DARK_PURPLE));
        out.setItemMeta(meta);
        return out;
    }
    
    public UUID getSender() {
        return sender;
    }
    
    public void setSender(UUID sender) {
        this.sender = sender;
    }
    
    public UUID getReceiver() {
        return receiver;
    }
    
    public void setReceiver(UUID receiver) {
        this.receiver = receiver;
    }
    
    public enum StampVariants {
        DEFAULT
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getItemReference(),new PostStamp().get());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.HONEYCOMB);
        return recipe;
    }
    
    public static NamespacedKey getPathingKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-pathing");
    }
    
    
    public static OfflinePlayer getSender(ItemStack sendableItem){
        try{
            Pair<UUID,UUID> couple = deserializeSenderAndReceiver(Objects.requireNonNull(sendableItem.getItemMeta().getPersistentDataContainer().get(getPathingKey(), PersistentDataType.STRING)));
            UUID sender = couple.getKey();
            return Bukkit.getOfflinePlayer(sender);
        } catch (NullPointerException n){
            return null;
        }
    }
    
    public static OfflinePlayer getReceiver(ItemStack sendableItem){
        try{
            Pair<UUID,UUID> couple = deserializeSenderAndReceiver(Objects.requireNonNull(sendableItem.getItemMeta().getPersistentDataContainer().get(getPathingKey(), PersistentDataType.STRING)));
            UUID sender = couple.getValue();
            return Bukkit.getOfflinePlayer(sender);
        } catch (NullPointerException n){
            return null;
        }
    }
    
    public static Pair<UUID,UUID> deserializeSenderAndReceiver(ItemStack source) throws ArrayIndexOutOfBoundsException, IllegalStateException{
        final String serializedData = source.getItemMeta().getPersistentDataContainer().get(getPathingKey(),PersistentDataType.STRING);
        if(serializedData == null) return null;
        if(!serializedData.contains(":")){
            throw new IllegalStateException("this string does not contain any stamp data");
        }
        final String senderString = serializedData.substring(0,serializedData.indexOf(":"));
        final String receiverString = serializedData.substring(serializedData.indexOf(":")+1);
        final UUID sender = UUID.fromString(senderString);
        final UUID receiver = UUID.fromString(receiverString);
        return Pair.of(sender,receiver);
    }
    
    private static Pair<UUID,UUID> deserializeSenderAndReceiver(String serializedData) throws ArrayIndexOutOfBoundsException, IllegalStateException{
        if(!serializedData.contains(":")){
            throw new IllegalStateException("this string does not contain any stamp data");
        }
        final String senderString = serializedData.substring(0,serializedData.indexOf(":"));
        final String receiverString = serializedData.substring(serializedData.indexOf(":")+1);
        final UUID sender = UUID.fromString(senderString);
        final UUID receiver = UUID.fromString(receiverString);
        return Pair.of(sender,receiver);
    }
    
    public static void setSenderAndReceiver(ItemStack item, UUID sender, UUID receiver){
        if(item != null){
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(getPathingKey(),PersistentDataType.STRING,generatePathString(sender,receiver));
            item.setItemMeta(meta);
        }
    }
    
    @EventHandler
    public static void anvilPathingCreate(PrepareAnvilEvent event){
        AnvilInventory inv = event.getInventory();
        ItemStack input = (inv.getFirstItem() != null && !inv.getFirstItem().getType().isAir()) ? inv.getFirstItem() : inv.getSecondItem();
        ItemStack result = inv.getResult();
        if(input == null || input.getType().isAir() || !TiboiseItem.getItemId(input).equals(new PostStamp().getId())){
            return;
        }
        
        final String userInput = inv.getRenameText();
        Pair<String,String> parsed = parsePathInput(userInput);
        if(parsed != null){
            final String senderName = PlayerDataManager.getRealName(parsed.getKey());
            final String receiverName = PlayerDataManager.getRealName(parsed.getValue());
            if(senderName != null && receiverName != null && result != null){
                result = result.clone();
                OfflinePlayer sender = Bukkit.getOfflinePlayerIfCached(senderName);
                OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(receiverName);
                if(sender == null || receiver == null) return;
                
                ItemMeta meta = result.getItemMeta();
                meta.displayName(Component.text("Stamp").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(buildLore(senderName,receiverName));
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(getPathingKey(),PersistentDataType.STRING,generatePathString(sender.getUniqueId(),receiver.getUniqueId()));
                final ItemStack finalResult = result;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        finalResult.setItemMeta(meta);
                        inv.setResult(finalResult);
                    }
                }.runTask(Tiboise.getPlugin());
                
            }
            
        }
        
    }
    
    private static Pair<String, String> parsePathInput(String input){
        final String cleanInput = input.replaceAll(" ","");
        String identifier = cleanInput.contains("->") ? "->" : cleanInput.contains(":") ? ":" : null;
        if(identifier == null || cleanInput.indexOf(identifier) == 0 || cleanInput.indexOf(identifier) == cleanInput.length()-identifier.length()) return null;
        return Pair.of(cleanInput.substring(0,cleanInput.indexOf(identifier)),cleanInput.substring(cleanInput.indexOf(identifier)+identifier.length()));
    }
    
    private static String generatePathString(UUID sender, UUID receiver){
        return sender.toString()+":"+receiver;
    }
    
    public static List<Component> buildLore(String senderName, String receiverName){
        return List.of(
                Component.text(" From : ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(senderName).color(NamedTextColor.GOLD)),
                Component.text(" to : ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(receiverName).color(NamedTextColor.GOLD))
        );
    }
}
