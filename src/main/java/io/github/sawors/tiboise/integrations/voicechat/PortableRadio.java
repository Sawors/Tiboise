package io.github.sawors.tiboise.integrations.voicechat;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PortableRadio extends FrequencyItem implements Listener {
    
    private Set<UUID> radiocache = new HashSet<>();
    // player UUID lifetime in the radio cache, in seconds
    private static final double cacheduration= 1;
    private static final int frequmax = 462900;
    private static final int frequmin = 462000;
    
    public PortableRadio(){
        setMaterial(Material.AMETHYST_SHARD);
        setFrequency(462000);
    }
    
    @Override
    public void setFrequency(int frequency) {
        super.setFrequency(frequency);
        setLore(getLoreBuild(frequency));
    }
    
    public static List<Component> getLoreBuild(int frequency){
        return List.of(
                Component.text(""),
                Component.text(ChatColor.DARK_GRAY+"Frequency : "+getFrequencyDisplay(frequency)+"MHz")
        );
    }
    
    
    public void copySendPacket(MicrophonePacketEvent event){
        
        if(Bukkit.getOnlinePlayers().size() <= 1){
            return;
        }
        
        // The connection might be null if the event is caused by other means
        if (event.getSenderConnection() == null) {
            return;
        }
        // Cast the generic player object of the voice chat API to an actual bukkit player
        // This object should always be a bukkit player object on bukkit based servers
        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) {
            return;
        }
        
        
        
        ItemStack emitter = TiboiseItem.getItemId(player.getInventory().getItemInMainHand()).equals(getId(PortableRadio.class)) ? player.getInventory().getItemInMainHand() : TiboiseItem.getItemId(player.getInventory().getItemInOffHand()).equals(new PortableRadio().getId()) ? player.getInventory().getItemInOffHand() : null;
        if(emitter == null){
            return;
        }
        int frequency = FrequencyItem.getItemFrequency(emitter);
        
        VoicechatServerApi api = event.getVoicechat();
        
        Location emitloc = player.getLocation();
        int maxdistance = Bukkit.getServer().getViewDistance()*2*16;
    
        //OpusDecoder decoder = api.createDecoder();
        // Iterating over every player on the server
        for (Player onlineplayer : Bukkit.getServer().getOnlinePlayers()) {
            // Don't send the audio to the player that is broadcasting
            if (onlineplayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            ItemStack receiver = TiboiseItem.getItemId(onlineplayer.getInventory().getItemInMainHand()).equals(getId(PortableRadio.class)) ? onlineplayer.getInventory().getItemInMainHand() : TiboiseItem.getItemId(onlineplayer.getInventory().getItemInOffHand()).equals(new PortableRadio().getId()) ? onlineplayer.getInventory().getItemInOffHand() : null;
            boolean transmit = false;
            if(radiocache.contains(onlineplayer.getUniqueId())){
                transmit = true;
            } else {
                if(receiver != null && TiboiseItem.getItemId(receiver).equals(getId(PortableRadio.class)) && FrequencyItem.getItemFrequency(receiver) == frequency){
                    transmit = true;
                    final UUID otherid = onlineplayer.getUniqueId();
                    // add player to cache
                    radiocache.add(otherid);
                    // remove player from cache after a duration
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            radiocache.remove(otherid);
                        }
                    }.runTaskLater(Tiboise.getPlugin(),(long)(cacheduration*20));
                }
            }
            if(transmit){
                if(onlineplayer.getLocation().distance(emitloc) > maxdistance){
                    continue;
                }
                VoicechatConnection connection = api.getConnectionOf(onlineplayer.getUniqueId());
                // Check if the player is actually connected to the voice chat
                if (connection == null) {
                    continue;
                }
                //short[] voiceaudio = decoder.decode(event.getPacket().getOpusEncodedData());
                //Tiboise.logAdmin("Audio packet length : "+voiceaudio.length);
    
                // Send a static audio packet of the microphone data to the connection of each player
                api.sendStaticSoundPacketTo(connection, event.getPacket().staticSoundPacketBuilder().build());
            }
            
        }
        
        //decoder.close();
    }
    
    @EventHandler
    public static void editFrequencyEvent(PlayerInteractEvent event){
        ItemStack radio = event.getPlayer().getInventory().getItemInMainHand();
        int newfrequency;
        if(TiboiseItem.getItemId(radio).equals(getId(PortableRadio.class))){
            newfrequency = (Math.min(Math.max(frequmin,FrequencyItem.getItemFrequency(radio)+(event.getAction().isLeftClick() ? -10 : 10)),frequmax));
            setItemFrequency(radio, newfrequency);
            radio.lore(getLoreBuild(newfrequency));
            event.getPlayer().sendActionBar(Component.text("Radio Frequency : "+ChatColor.YELLOW+getFrequencyDisplay(newfrequency)+"MHz"));
            
            event.getPlayer().playSound(event.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,.5f,event.getAction().isLeftClick() ? .8f : 1);
        }
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Tiboise.getPlugin(),getId()),new PortableRadio().get());
        recipe.shape(
                "XLX",
                "CAC",
                "XRX"
        );
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('L', Material.LIGHTNING_ROD);
        recipe.setIngredient('X', Material.AIR);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
