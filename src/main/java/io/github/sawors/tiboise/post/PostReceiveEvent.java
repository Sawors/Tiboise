package io.github.sawors.tiboise.post;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class PostReceiveEvent extends BlockEvent {
    
    private final PostTransitPackage postPackage;
    private final PostLetterBox target;
    private final boolean sentBack;
    private final boolean delivered;
    
    public PostReceiveEvent(Block deliveredTo, PostTransitPackage postPackage, PostLetterBox target, boolean sentBack, boolean delivered) {
        super(deliveredTo);
        this.postPackage = postPackage;
        this.target = target;
        this.sentBack = sentBack;
        this.delivered = delivered;
    }
    
    public PostTransitPackage getPostPackage() {
        return postPackage;
    }
    
    public PostLetterBox getTarget() {
        return target;
    }
    
    public boolean isSentBack() {
        return sentBack;
    }
    
    public boolean isDelivered() {
        return delivered;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return new HandlerList();
    }
}
