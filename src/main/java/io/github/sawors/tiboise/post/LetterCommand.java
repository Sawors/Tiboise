package io.github.sawors.tiboise.post;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LetterCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p){
            final Component letter = PostLetter.getLastLetter(p.getUniqueId());
            p.sendMessage(Component.text("Your last letter :\n").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD).append(letter.decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)));
            return true;
        }
        return false;
    }
}
