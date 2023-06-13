package io.github.sawors.tiboise.items.discs;

import io.github.sawors.tiboise.core.LocalResourcesManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class DiscCommand implements TabExecutor {
    
    private static final List<ItemStack> available = new ArrayList<>();
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length >= 1){
            String sub1 = args[0];
            switch (sub1){
                case "create" -> {
                    if(args.length >= 2){
                        String url = args[1];
                        try {
                            MusicDisc.buildFromSource(new URL(url), d -> {
                                sender.sendMessage(Component.text("Created disc with id : "+d.getTitleHash()).clickEvent(ClickEvent.suggestCommand("/disc give "+d.getTitleHash())).color(NamedTextColor.LIGHT_PURPLE));
                                LocalResourcesManager.rebuildResourcePack();
                            });
                        } catch (MalformedURLException e) {
                            sender.sendMessage(Component.text("The url provided is not a correct url !"));
                        }
                        
                        return true;
                    }
                }
                case "list" -> {
                    Map<String,String> names = MusicDisc.getIndexedMusics();
                    for(String title : names.values()){
                        Pair<String,String> info = MusicDisc.parseTitleString(title);
                        available.add(new MusicDisc(info.getKey(),info.getValue()).get());
                    }
                    
                    Map<Integer,Inventory> listPages = new HashMap<>();
                    
                    int page = 0;
                    final int invSize = 9*6;
                    for(int i = 0; i<available.size();i++){
                        ItemStack item = available.get(i);
                        int index = (i+1)%invSize;
                        if(index == 0) page++;
                        // no caching
                        Inventory content =  Bukkit.createInventory(null,invSize,Component.text("Discs Page "+page+1));
                        content.addItem(item);
                        listPages.put(page,content);
                    }
                    
                    int queriedPage = 1;
                    if(args.length >= 2) queriedPage = Integer.parseInt(args[1]);
                    Inventory inv = listPages.getOrDefault(queriedPage-1, Bukkit.createInventory(null,invSize,Component.text("Discs Page "+page+1)));
                    if(sender instanceof Player p){
                        p.openInventory(inv);
                    }
                    
                    return true;
                }
                
                case "get", "give" -> {
                    if(sender instanceof Player player){
                        String name = Arrays.stream(args).reduce((c1, c2) -> c1+" "+c2).get().replaceFirst(args[0]+" ","");
                        String lookup = MusicDisc.lookupMusicName(name);
                        if(lookup == null && (name.startsWith("-") || !name.contains("-"))){
                            name = "Unknown - Unknown";
                        }
                        Pair<String,String> musicData = MusicDisc.parseTitleString(lookup != null ? lookup : name);
                        MusicDisc item = new MusicDisc(musicData.getKey(),musicData.getValue());
                        player.getInventory().addItem(item.get());
                        
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1){
            return List.of("create","list","get");
        } else if(args.length == 2 && args[0].equals("get")){
            return new ArrayList<>(MusicDisc.getIndexedMusics().keySet());
        }
        return null;
    }
}
