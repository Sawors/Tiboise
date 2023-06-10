package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import io.github.sawors.tiboise.items.discs.MusicDisc;
import io.github.sawors.tiboise.items.utility.security.IdentifiedItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GetIdCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            ItemStack item = p.getInventory().getItemInMainHand();
            String itemidentifier = IdentifiedItem.getItemId(item);
            if(item.getType().equals(Material.STICK)){
                Entity e = p.getTargetEntity(8);
                if(e instanceof ArmorStand am){
                    am.setItem(EquipmentSlot.HEAD,item);
                }
                return true;
            }
            if(itemidentifier != null && item.hasItemMeta()){
                p.sendMessage("The identifier of item ["+ Tiboise.getComponentContent(item.displayName())+"] is "+itemidentifier);
            } else {
                p.sendMessage("This item has no identifier");
            }
            
            if(TiboiseItem.getItemId(item).equals(TiboiseItem.getId(MusicDisc.class))){
                String hash = Objects.requireNonNull(item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Tiboise.getPlugin(), "music-disc-hash"), PersistentDataType.STRING)).replaceAll("minecraft:tiboise.music_disc.","");
                p.sendMessage(Component.text("Hash of the disc : "+hash).color(NamedTextColor.GRAY).clickEvent(ClickEvent.copyToClipboard(hash)));
            }
            return true;
        }
        return false;
    }
}
