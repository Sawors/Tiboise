package io.github.sawors.tiboise.post;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PostSendEvent extends PlayerEvent {
    
    private final PostTransitPackage postPackage;
    private final Block source;
    
    public PostSendEvent(@NotNull Player sender, PostTransitPackage postPackage, Block source) {
        super(sender);
        this.postPackage = postPackage;
        this.source = source;
    }
    
    public PostTransitPackage getPostPackage() {
        return postPackage;
    }
    
    public Block getSource() {
        return source;
    }
    
    public Player getSender() {
        return getPlayer();
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}
