package io.github.sawors.tiboise.fishing;

import io.github.sawors.tiboise.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FishingManager implements Listener {

    private static Map<String, FishReference> fishmap = new HashMap<>();
    private static Map<String, FishReference> legendaryfishmap = new HashMap<>();
    private static Map<String, String> watertypemap = new HashMap<>();


    public static void loadFishVariants(){
        File basedir = new File(Main.getPlugin().getDataFolder()+ File.separator+"fishing");
        File variants = new File(basedir+File.separator+"fish_variants.yml");
        if(!variants.exists()){
            try(InputStream in = Main.getPlugin().getResource("fishing/fish_variants.yml"); FileOutputStream out = new FileOutputStream(variants)){
                if(in != null){
                    variants.createNewFile();
                    out.write(in.readAllBytes());
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(variants);
        for(String key : data.getKeys(false)){
            ConfigurationSection section = data.getConfigurationSection(key);
            if(section != null){
                String name = key;
                double maxweight = section.getDouble("max-weight");
                double minweight = section.getDouble("min-weight");
                double rarity = section.getDouble("spawn-rarity");
                List<?> biomes = section.getList("spawn");

                fishmap.put(name, new FishReference());

            }
        }
    }


    public static void loadLegendaryFishVariants(){
        File basedir = new File(Main.getPlugin().getDataFolder()+ File.separator+"fishing");
        File legendary = new File(basedir+File.separator+"legendary_fish_variants.yml");
        if(!legendary.exists()){
            try(InputStream in = Main.getPlugin().getResource("fishing/legendary_fish_variants.yml"); FileOutputStream out = new FileOutputStream(legendary)){
                if(in != null){
                    legendary.createNewFile();
                    out.write(in.readAllBytes());
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void loadWaterZones(){
        File basedir = new File(Main.getPlugin().getDataFolder()+ File.separator+"fishing");
        File watertypes = new File(basedir+File.separator+"water_types.yml");
        if(!watertypes.exists()){
            try(InputStream in = Main.getPlugin().getResource("fishing/water_types.yml"); FileOutputStream out = new FileOutputStream(watertypes)){
                if(in != null){
                    watertypes.createNewFile();
                    out.write(in.readAllBytes());
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
