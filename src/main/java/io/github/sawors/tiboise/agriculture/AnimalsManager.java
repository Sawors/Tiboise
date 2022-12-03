package io.github.sawors.tiboise.agriculture;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.TiboiseUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AnimalsManager implements Listener {
    
    private static Set<UUID> arthuragornChickens = new HashSet<>();
    
    @EventHandler
    public static void duckChance(EntityBreedEvent event){
        Entity e = event.getBreeder();
        if(e instanceof Chicken && (event.getMother().getName().equalsIgnoreCase("duck") || event.getFather().getName().equalsIgnoreCase("duck")) && Math.random() >= .01){
            event.getEntity().customName(Component.text("Duck"));
        }
    }
    
    
    @EventHandler
    public static void logChickens(EntityAddToWorldEvent event){
        if(event.getEntity() instanceof Chicken c && c.getName().equals("Arthuragorn")){
            arthuragornChickens.add(event.getEntity().getUniqueId());
        }
    }
    
    
    //
    //      PACKETS
    //
    static PacketAdapter blockprojectiles = new PacketAdapter(Main.getPlugin(), PacketType.Play.Server.SPAWN_ENTITY) {
        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            UUID entityID = packet.getUUIDs().read(0);
            int entid = packet.getIntegers().read(0);
            
            if(packet.getType() == PacketType.Play.Server.SPAWN_ENTITY){
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(packet.getEntityTypeModifier().read(0) == EntityType.CHICKEN && arthuragornChickens.contains(packet.getUUIDs().readSafely(0))){
        
                            PacketContainer spawn = packet.shallowClone();
        
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    spawn.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
                                    spawn.getUUIDs().write(0, entityID);
                                    spawn.getIntegers().write(0, entid);
                                    try {
                                        Main.getProtocolManager().sendServerPacket(event.getPlayer(), spawn);
                                    } catch (
                                            InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                    new BukkitRunnable(){
                    
                                        @Override
                                        public void run() {
                                            // send equipment
                                            PacketContainer equipment = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
                                            equipment.getIntegers().write(0,spawn.getIntegers().read(0));
                                            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                                            SkullMeta meta = (SkullMeta) head.getItemMeta();
                                            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString("6425fa9b-81b3-4f3d-b71c-ce1d1f41fd08")));
                                            head.setItemMeta(meta);
                                            Pair<EnumWrappers.ItemSlot, ItemStack> pair = new Pair<>(EnumWrappers.ItemSlot.HEAD, head);
                                            ArrayList<Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
                                            list.add(pair);
                                            equipment.getSlotStackPairLists().write(0, list);
                                            try {
                                                Main.getProtocolManager().sendServerPacket(event.getPlayer(), equipment);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                        
                        
                                            // send data (invisibility here)
                                            PacketContainer packet = Main.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
                                            packet.getIntegers().write(0, entid); //Set packet's entity id
                                            WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
                                            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class); //Found this through google, needed for some stupid reason
                                            //watcher.setEntity(player); //Set the new data watcher's target
                                            watcher.setObject(0, serializer, (byte) (0x20)); //Set status to glowing, found on protocol page
                                            watcher.setObject(15, serializer, (byte) (0x01)); //Set status to glowing, found on protocol page
                                            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created
                                            try {
                                                Main.getProtocolManager().sendServerPacket(event.getPlayer(), packet);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.runTaskLater(Main.getPlugin(), 1);
                                }
                            }.runTaskLater(Main.getPlugin(), 1);
                        }
                    }
                }.runTaskLater(Main.getPlugin(),1);
            }
        }
    };
    
    static PacketAdapter adapterSpawn = new PacketAdapter(Main.getPlugin(), PacketType.Play.Server.SPAWN_ENTITY) {
        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            Main.logAdmin("\n"+ChatColor.GOLD+PacketType.Play.Server.SPAWN_ENTITY);
            for(String str : TiboiseUtils.getPacketContentPrint(packet)){
                Main.logAdmin(str);
            }
        }
    };
    static PacketAdapter adapterMeta = new PacketAdapter(Main.getPlugin(), PacketType.Play.Server.ENTITY_METADATA) {
        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            Main.logAdmin("\n"+ChatColor.GOLD+packet.getType());
            for(String str : TiboiseUtils.getPacketContentPrint(packet)){
                Main.logAdmin(str);
            }
        }
    };
    static PacketAdapter adapterStatus = new PacketAdapter(Main.getPlugin(), PacketType.Play.Server.ENTITY_STATUS) {
        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            Main.logAdmin("\n"+ChatColor.GOLD+packet.getType());
            for(String str : TiboiseUtils.getPacketContentPrint(packet)){
                Main.logAdmin(str);
            }
        }
    };
    static PacketAdapter adapterEffects = new PacketAdapter(Main.getPlugin(), PacketType.Play.Server.ENTITY_EFFECT) {
        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            Main.logAdmin("\n"+ChatColor.GOLD+packet.getType());
            for(String str : TiboiseUtils.getPacketContentPrint(packet)){
                Main.logAdmin(str);
            }
        }
    };
    static PacketAdapter adapterEquipment = new PacketAdapter(Main.getPlugin(), PacketType.Play.Server.ENTITY_EQUIPMENT) {
        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            Main.logAdmin("\n"+ChatColor.GOLD+packet.getType());
            for(String str : TiboiseUtils.getPacketContentPrint(packet)){
                Main.logAdmin(str);
            }
        }
    };
    @EventHandler(priority = EventPriority.HIGH)
    public static void onEnable(PluginEnableEvent event){
        Main.getProtocolManager().addPacketListener(blockprojectiles);
//        Main.getProtocolManager().addPacketListener(adapterSpawn);
//        Main.getProtocolManager().addPacketListener(adapterEffects);
//        Main.getProtocolManager().addPacketListener(adapterEquipment);
        //Main.getProtocolManager().addPacketListener(adapterMeta);
//        Main.getProtocolManager().addPacketListener(adapterStatus);
    }
}
