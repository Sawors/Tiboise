package io.github.sawors.tiboise.economy;

import io.github.sawors.tiboise.ConfigModules;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.ItemTag;
import io.github.sawors.tiboise.core.TiboiseItem;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.*;

public class CoinItem extends TiboiseItem implements Listener {

    private int value = 5;
    private static Map<String, Integer> coinvalues = new HashMap<>();
    private static Map<String, String> coincolors = new HashMap<>();

    public CoinItem() {
        super();
        setCoinBaseAttributes();

    }

    public CoinItem(int value){
        setCoinBaseAttributes();
        setCoinValue(value);
    }

    public CoinItem(String variant){
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
            setDisplayName(Component.text(""));
        }
    }

    private void setCoinBaseAttributes(){
        setMaterial(Material.GOLD_NUGGET);
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
    }

    private void setCoinValue(int value){
        this.value = value;
        this.setLore(List.of(Component.text("\n"+ ChatColor.GRAY+"a coin with a value of "+value).asComponent()));
    }

    public static NamespacedKey getCoinValueKey(){
        return new NamespacedKey((Tiboise.getPlugin(Tiboise.class)), "value");
    }

    private void setCoinName(){

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
                    int val = coinsection.getInt(key);
                    String color = coinsection.getString("color") != null ? coinsection.getString("color") : "WHITE";

                    if(val >= 0){
                        coinvalues.put(key, val);
                        coincolors.put(key, color);
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
