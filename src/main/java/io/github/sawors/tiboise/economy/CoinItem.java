package io.github.sawors.tiboise.economy;

import io.github.sawors.tiboise.ConfigModules;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.ItemTag;
import io.github.sawors.tiboise.items.IdentifiedItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.awt.*;
import java.util.List;
import java.util.*;

public class CoinItem extends IdentifiedItem implements Listener {

    private static Map<String, Integer> coinvalues = new HashMap<>();
    private static Map<String, String> coincolors = new HashMap<>();

    public CoinItem() {
        super();
        setCoinBaseAttributes();

    }

    public CoinItem(int value){
        super();
        setCoinBaseAttributes();
        setCoinValue(value);
    }

    public CoinItem(String variant){
        super();
        setCoinBaseAttributes();
        setCoinVariant(variant);
    }

    private void setCoinBaseAttributes(){
        setMaterial(Material.GOLD_NUGGET);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setIdentifier(UUID.randomUUID());
    }

    public void setCoinValue(int value){
        this.setLore(List.of(Component.text(""),Component.text(ChatColor.GRAY+"a coin with a value of "+value).asComponent()));
        addData(getCoinValueKey(), String.valueOf(value));
    }

    public void setCoinVariant(String variant){
        int value = -1;
        String name = null;
        String ref = variant.toLowerCase(Locale.ROOT);

        for(String check : coinvalues.keySet()){
            if (check.toLowerCase(Locale.ROOT).equals(ref)) {
                name = check;
                value = coinvalues.get(check);
            }
        }

        if(name != null && name.length() > 0 && value >= 0){
            setCoinValue(value);
            setDisplayName(buildCoinName(variant));
        }
    }

    public static NamespacedKey getCoinValueKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "value");
    }

    private Component buildCoinName(String variant){
        Component result = Component.text(ChatColor.DARK_GRAY+"Unknown Coin");
        String formattedvariant = variant.toLowerCase(Locale.ROOT);
        String name = null;
        int value = 0;
        String color = "WHITE";
        int rgb = 0xFFFFFF;

        if(coinvalues.containsKey(formattedvariant)){
            value = coinvalues.get(formattedvariant);
        }
        if(coincolors.containsKey(formattedvariant)){
            color = coincolors.get(formattedvariant);
        }

        name = Character.toUpperCase(formattedvariant.charAt(0))+formattedvariant.substring(1);

        if(StringUtils.isAlphanumeric(color.replaceAll("_", ""))){
            String upcolor = color.toUpperCase(Locale.ROOT);
            switch(upcolor){
                case "IRIDESCENT" -> {
                    char[] chars = (name+" Coin").toCharArray();
                    result = Component.translatable("");
                    for(char c : chars){
                        Component coloredletter = Component.translatable(String.valueOf(c));
                        result = result.append(coloredletter.color(TextColor.color(getRandomIridescentColor())));
                    }

                    return result;
                }
                default -> {rgb = translateColorString(upcolor);}
            }
        } else if(color.contains("0x") || color.contains("#")){
            rgb =  Integer.parseInt(color.replaceFirst("#","").replaceFirst("0x",""),16);
        }

        result = Component.text(name+" Coin").color(TextColor.color(rgb)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        return result;
    }

    public int translateColorString(String color){
        switch(color.toUpperCase(Locale.ROOT)){
            case "DARK_RED" -> {return 0xAA0000;}
            case "RED" -> {return 0xFF5555;}
            case "GOLD" -> {return 0xFFAA00;}
            case "YELLOW" -> {return 0xFFFF55;}
            case "DARK_GREEN" -> {return 0x00AA00;}
            case "GREEN" -> {return 0x55FF55;}
            case "AQUA" -> {return 0x55FFFF;}
            case "DARK_AQUA" -> {return 0x00AAAA;}
            case "DARK_BLUE" -> {return 0x0000AA;}
            case "BLUE" -> {return 0x5555FF;}
            case "PURPLE" -> {return 0xFF55FF;}
            case "DARK_PURPLE" -> {return 0xAA00AA;}
            case "WHITE" -> {return 0xFFFFFF;}
            case "GRAY" -> {return 0xAAAAAA;}
            case "DARK_GRAY" -> {return 0x555555;}
            case "BLACK" -> {return 0x000000;}
            default -> {return 0xFFFFFF;}
        }
    }

    private int getRandomIridescentColor(){
        float h = (float) Math.random();
        float s = (float) (Math.random()*.10f)+.05f;
        float b = 1;
        return Color.getHSBColor(h,s,b).getRGB();
    }


    @EventHandler
    public static void loadCoinValues(ServerLoadEvent event){
        try{
            ConfigurationSection valuesection = Objects.requireNonNull(Tiboise.getModuleSection(ConfigModules.ECONOMY)).getConfigurationSection("coin-values");
            Set<String> values = Objects.requireNonNull(valuesection).getKeys(false);

            Tiboise.logAdmin("Loading coin values...");

            for(String key : values){
                ConfigurationSection coinsection = valuesection.getConfigurationSection(key);
                if(coinsection != null){
                    int val = coinsection.getInt("value");
                    String color = coinsection.getString("color") != null ? coinsection.getString("color") : "WHITE";

                    if(val >= 0){
                        coinvalues.put(key.toLowerCase(Locale.ROOT), val);
                        coincolors.put(key.toLowerCase(Locale.ROOT), color);
                    }
                    Tiboise.logAdmin("Loaded coin "+key+" = "+val+" ("+color+")");
                }
            }

            Tiboise.logAdmin("Finished loading coin values");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}
