package io.github.sawors.tiboise.post;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public abstract class SendableItem extends TiboiseItem {
    
    private UUID sender;
    private UUID receiver;
    private PostStamp stamp;
    
    public SendableItem(){
    
    }
    
    public static NamespacedKey getStampKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-stamp");
    }
    public static NamespacedKey getPathingKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"post-pathing");
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
    
    public PostStamp getStamp() {
        return stamp;
    }
    
    public void setStamp(PostStamp stamp) {
        this.stamp = stamp;
    }
    
    public static OfflinePlayer getSender(ItemStack sendableItem){
        Pair<UUID,UUID> couple = deserializeSenderAndReceiver(Objects.requireNonNull(sendableItem.getItemMeta().getPersistentDataContainer().get(getPathingKey(), PersistentDataType.STRING)));
        UUID sender = couple.getKey();
        return Bukkit.getOfflinePlayer(sender);
    }
    
    public static OfflinePlayer getReceiver(ItemStack sendableItem){
        Pair<UUID,UUID> couple = deserializeSenderAndReceiver(Objects.requireNonNull(sendableItem.getItemMeta().getPersistentDataContainer().get(getPathingKey(), PersistentDataType.STRING)));
        UUID sender = couple.getValue();
        return Bukkit.getOfflinePlayer(sender);
    }
    
    private static Pair<UUID,UUID> deserializeSenderAndReceiver(String serializedData){
        final UUID sender = UUID.fromString(serializedData.substring(0,serializedData.indexOf(":")));
        logAdmin(sender);
        final UUID receiver = UUID.fromString(serializedData.substring(serializedData.indexOf(":")));
        logAdmin(receiver);
        
        return Pair.of(sender,receiver);
    }
}
