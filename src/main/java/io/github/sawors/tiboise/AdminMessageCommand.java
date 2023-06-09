package io.github.sawors.tiboise;

import net.dv8tion.jda.api.JDA;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;

public class AdminMessageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length > 1){
            final String destination = args[0];
            final String message = Arrays.stream(args).reduce((c1,c2) -> c1+c2).get();
            if((destination.equalsIgnoreCase("admin") || destination.equalsIgnoreCase("server") || destination.equalsIgnoreCase("serveur")) && sender instanceof Player player){
                final JDA jda = Tiboise.getJdaInstance();
                final String formattedMessage = "Message from **"+player.getName()+"** :\n```"+message+"```";
                if(jda != null){
                    jda.openPrivateChannelById("315237447065927691").queue(s -> s.sendMessage(formattedMessage).queue());
                } else {
                    if(Bukkit.getOnlinePlayers().stream().anyMatch(ServerOperator::isOp)){
                        Bukkit.getOnlinePlayers().forEach(op -> op.sendMessage(
                                Component.text("Message from "+player.getName()+" :\n").color(NamedTextColor.GOLD)
                                        .append(Component.text(message).color(NamedTextColor.WHITE))
                                        .clickEvent(ClickEvent.suggestCommand("/mp "+player.getName()+" "))
                        ));
                    } else {
                        Bukkit.getLogger().log(Level.INFO,formattedMessage.replaceAll("`","").replaceAll("\\*",""));
                    }
                }
            } else {
                OfflinePlayer p = Bukkit.getOfflinePlayerIfCached(destination);
                if(p != null && p.isOnline()){
                    ((Player) p).sendMessage(Component.text("Message from the server :\n").color(NamedTextColor.GOLD).append(Component.text(message).color(NamedTextColor.WHITE))
                            .clickEvent(ClickEvent.suggestCommand("/mp server "))
                            .hoverEvent(Component.text("> click to respond <"))
                    );
                }
            }
        }
        return false;
    }
}
