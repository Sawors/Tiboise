package io.github.sawors.tiboise.economy;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class CoinItem extends TiboiseItem implements Listener {

    private static Map<String, Integer> coinvalues = new HashMap<>();
    private static Map<String, String> coincolors = new HashMap<>();
    // UUID : Player, UUID : Item (coin)
    private static Map<UUID, UUID> coinflippers = new HashMap<>();
    
    @Override
    public void onRegister() {
        // generate conversion crafts
        List<String> sortedCoins = new ArrayList<>(coinvalues.keySet());
        sortedCoins.sort(Comparator.comparingInt(c -> coinvalues.get(c)));
        logAdmin(sortedCoins);
        for(int i = 0; i<sortedCoins.size(); i++){
            final String actual = sortedCoins.get(i);
            final int actualValue = coinvalues.get(actual);
            String previous = null;
            if(i > 0){
                previous = sortedCoins.get(i-1);
            }
            
            // add conversion recipes
            if(previous != null){
                int previousValue = coinvalues.get(previous);
                if(previousValue == 0) return;
                final int quantity = Math.floorDiv(actualValue,previousValue);
                if(quantity > 64 || quantity < 1){
                    Bukkit.getLogger().log(Level.WARNING, "Could not create the recipe to convert "+previous+" coin to "+actual+" coin (conversion impossible within vanilla stack limits)");
                } else {
                    ShapelessRecipe recipeUp = new ShapelessRecipe(new NamespacedKey(Tiboise.getPlugin(),previous.toLowerCase(Locale.ROOT)+"_coin"+"_to_"+actual.toLowerCase(Locale.ROOT)+"_coin"), new CoinItem(actual).get().asQuantity(1));
                    recipeUp.addIngredient(new CoinItem(previous).get().asQuantity(quantity));
                    ShapelessRecipe recipeDown = new ShapelessRecipe(new NamespacedKey(Tiboise.getPlugin(),actual.toLowerCase(Locale.ROOT)+"_coin"+"_to_"+previous.toLowerCase(Locale.ROOT)+"_coin"), new CoinItem(previous).get().asQuantity(quantity));
                    recipeDown.addIngredient(new CoinItem(actual).get().asQuantity(1));
                    try{
                        Bukkit.addRecipe(recipeUp);
                        Bukkit.addRecipe(recipeDown);
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                
            }
        }
    }
    
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
        setVariant(variant);
    }

    private void setCoinBaseAttributes(){
        setMaterial(Material.GOLD_NUGGET);
        setId("coin");
    }
    
    @Override
    public void setVariant(String variant) {
        super.setVariant(variant);
        setCoinVariant(variant);
    }
    
    public void setCoinValue(int value){
        this.setLore(List.of(Component.text("Value : "+value+"c").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
        addData(getCoinValueKey(), String.valueOf(value));
    }
    
    public static int getCoinValue(String name){
        return coinvalues.get(name);
    }

    private void setCoinVariant(String variant){
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
        String color = "WHITE";
        int rgb = 0xFFFFFF;
        
        if(coincolors.containsKey(formattedvariant)){
            color = coincolors.get(formattedvariant);
        }

        name = Character.toUpperCase(formattedvariant.charAt(0))+formattedvariant.substring(1);

        if(StringUtils.isAlphanumeric(color.replaceAll("_", ""))){
            String upcolor = color.toUpperCase(Locale.ROOT);
            switch(upcolor){
                case "IRIDESCENT" -> {
                    /*char[] chars = (name+" Coin").toCharArray();
                    result = Component.text("");
                    int mid = ((name+" Coin").replaceAll(" ","").toCharArray().length-1)/2;
                    float h = 247f/360;
                    float b = 1;
                    float sref = .3f;
                    float s = 0;
                    for(int i = 0; i<mid;i++){
                        result = result.append(Component.text(chars[i]).color(TextColor.color(Color.getHSBColor(h,s,b).getRGB())));
                        s+=sref/(mid);
                        s = Math.min(s,sref);
                    }
                    result = result.append(Component.text(chars[mid]).color(TextColor.color(0xE5D43B)));
                    Tiboise.logAdmin("mid");
                    for(int i = mid+1; i<chars.length;i++){
                        Tiboise.logAdmin(s);
                        result = result.append(Component.text(chars[i]).color(TextColor.color(Color.getHSBColor(h,s,b).getRGB())));
                        s -= sref/(mid);
                        s = Math.max(0,s);
                    }
*/
                    return result.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
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

    /*private int getRandomIridescentColor(){
        float h = (float) Math.random();
        float s = (float) (Math.random()*.15f)+.05f;
        float b = 1;
        return Color.getHSBColor(h,s,b).getRGB();
    }*/

    public static void loadCoinValues(){
        try{
            ConfigurationSection valuesection = Objects.requireNonNull(Tiboise.getModuleSection(Tiboise.ConfigModules.ECONOMY)).getConfigurationSection("coin-values");
            Set<String> values = Objects.requireNonNull(valuesection).getKeys(false);

            logAdmin("Loading coin values...");

            for(String key : values){
                ConfigurationSection coinsection = valuesection.getConfigurationSection(key);
                if(coinsection != null){
                    int val = coinsection.getInt("value");
                    String color = coinsection.getString("color") != null ? coinsection.getString("color") : "WHITE";

                    if(val >= 0){
                        coinvalues.put(key.toLowerCase(Locale.ROOT), val);
                        coincolors.put(key.toLowerCase(Locale.ROOT), color);
                    }
                    logAdmin("Loaded coin "+key+" = "+val+" ("+color+")");
                }
            }

            logAdmin("Finished loading coin values");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    
    @EventHandler
    public static void coinFlip(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();
        Player p = event.getPlayer();
        if(p.getLocation().getPitch() <= -80 && TiboiseItem.getItemId(item).equals(new CoinItem().getId())){
            final UUID coinid = event.getItemDrop().getUniqueId();
            coinflippers.put(p.getUniqueId(), coinid);
            new BukkitRunnable(){
                @Override
                public void run() {
                    if(coinflippers.containsKey(p.getUniqueId()) && coinflippers.get(p.getUniqueId()).equals(coinid)){
                        coinflippers.remove(p.getUniqueId());
                    }
                }
            }.runTaskLater(Tiboise.getPlugin(), 20*5);
        }
    }
    
    @EventHandler
    public static void coinFlipPickup(PlayerAttemptPickupItemEvent event){
        ItemStack item = event.getItem().getItemStack();
        Player p = event.getPlayer();
        if(coinflippers.containsKey(p.getUniqueId()) && TiboiseItem.getItemId(item).equals(new CoinItem().getId())){
            coinflippers.remove(p.getUniqueId());
            String result = Math.random() >= .5 ? "Pile" : "Face";
            p.sendMessage(Component.text("Et... C'est "+result+" !").color(TextColor.color(Color.ORANGE.getRGB())).decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE));
        }
    }
    
    public static List<ItemStack> getCoinsForValue(int value){
        List<ItemStack> resultarray = new ArrayList<>();
        int remainder = value;
        Map<Integer, String> reversecoinvalues = new HashMap<>();
        coinvalues.forEach((coinname, val) -> {reversecoinvalues.put(val,coinname);});
        ArrayList<Integer> orderedcoins = new ArrayList<>(reversecoinvalues.keySet());
        // ASCENDING order
        if(orderedcoins.size() >= 1){
            Collections.sort(orderedcoins);
            int smallestcoin = orderedcoins.get(0);
            int biggestcoin = orderedcoins.get(orderedcoins.size()-1);
            int index = orderedcoins.size()-1;
            while(remainder >= smallestcoin){
                if(remainder - orderedcoins.get(index) >= 0){
                    resultarray.add(new CoinItem(reversecoinvalues.get(orderedcoins.get(index))).get());
                    remainder-=orderedcoins.get(index);
                } else {
                    if(index-1 >= 0){
                        index -= 1;
                    } else {
                        break;
                    }
                }
            }
        }
        
        return resultarray;
    }
}
