package io.github.sawors.tiboise;

import net.dv8tion.jda.api.JDA;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class AdminMessageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length > 1){
            final String destination = args[0];
            final String message = Arrays.stream(args).reduce((c1,c2) -> c1+" "+c2).get().replaceFirst(destination,"");
            if((destination.equalsIgnoreCase("admin") || destination.equalsIgnoreCase("server") || destination.equalsIgnoreCase("serveur")) && sender instanceof Player player) {
                final JDA jda = Tiboise.getJdaInstance();
                final String formattedMessage = "Message from **"+player.getName()+"** :\n```"+message+"```";
                if(jda != null){
                    try{
                        jda.openPrivateChannelById(315237447065927691L).queue(s -> s.sendMessage(formattedMessage).queue());
                        sender.sendMessage(Component.text("You -> Server : message sent"));
                    } catch (UnsupportedOperationException e){
                        logAdmin("Message couldn't be sent to Sawors (Discord)");
                    }
                } else {
                    if(Bukkit.getOnlinePlayers().stream().anyMatch(ServerOperator::isOp)){
                        Bukkit.getOnlinePlayers().forEach(op -> {
                            op.sendMessage(
                                    Component.text("Message from "+player.getName()+" : ").color(NamedTextColor.LIGHT_PURPLE)
                                            .append(Component.text(message).color(NamedTextColor.WHITE))
                                            .clickEvent(ClickEvent.suggestCommand("/mp "+player.getName()+" "))
                            );
                            op.playSound(op.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
                        });
                    } else {
                        Bukkit.getLogger().log(Level.INFO,formattedMessage.replaceAll("`","").replaceAll("\\*",""));
                    }
                }
                return true;
            } else {
                OfflinePlayer p = Bukkit.getOfflinePlayerIfCached(destination);
                if(sender.isOp() && p != null && p.isOnline()){
                    final Player onlinePlayer = ((Player) p);
                    sender.sendMessage(Component.text("Server -> "+onlinePlayer.getName()+" : message sent"));
                    onlinePlayer.sendMessage(Component.text("Server :").color(NamedTextColor.LIGHT_PURPLE).append(Component.text(message).color(NamedTextColor.WHITE))
                            .clickEvent(ClickEvent.suggestCommand("/mp server "))
                            .hoverEvent(Component.text("> click to respond <"))
                    );
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
                } else {
                    sender.sendMessage(Component.text("You can't send messages to this user"));
                }
                return true;
            }
        }
        return false;
    }
}
