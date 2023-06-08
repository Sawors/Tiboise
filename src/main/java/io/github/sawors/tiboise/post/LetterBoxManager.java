package io.github.sawors.tiboise.post;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class LetterBoxManager implements Listener {
    
    private final static String signIdentifier = "- Poste -";
    
    
    @EventHandler
    public static void recogniseLetterBox(SignChangeEvent event){
        final Block b = event.getBlock();
        final Player p = event.getPlayer();
        if(b.getState() instanceof Sign sign){
            if(sign.lines().size() >= 1){
                final Component identifier = sign.line(1);
                if(((TextComponent) identifier).content().equals(signIdentifier)){
                    // sign is recognised as a post sign
                    if(sign.lines().size() >= 3){
                        final Component owner = sign.line(2);
                        final Component houseName = sign.line(3);
                        
                    }
                }
            }
        }
    }
}
