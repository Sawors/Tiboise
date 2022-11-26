package io.github.sawors.tiboise.exploration.items;

import io.github.sawors.tiboise.exploration.PlayerCompassMarker;
import io.github.sawors.tiboise.gui.GUIDisplayItem;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class CopperCompass extends TiboiseItem implements Listener {
    
    public final static String MARKER_INVENTORY_NAME = "Markers";
    public final static int MARKER_INVENTORY_ROW_AMOUNT = 6;
    // TODO : move this to the future DisplayItem class
    private static Map<Inventory, PlayerCompassMarker> inventoryLink = new HashMap<>();
    private static Map<Inventory, MarkerInventoryType> inventoryTypeLink = new HashMap<>();
    
    private static GUIDisplayItem backButtonFactory = new GUIDisplayItem(MarkerOptionsButton.BACK.toString(),Material.INK_SAC)
            .setName(Component.text(ChatColor.GOLD + "← Go Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            .setLore(List.of(Component.text(ChatColor.YELLOW+">> Click go back to the previous menu <<")));


    private enum MarkerInventoryType{
        LIST_DISPLAY, OPTIONS, ICONS
    }

    private enum MarkerOptionsButton{
        RENAME, DELETE, ADD_NEW, CHANGE_ICON, BACK, SET_ICON
    }

    public CopperCompass(){
        setMaterial(Material.COMPASS);
    }
    
    @EventHandler
    public static void openMarkerInventory(PlayerInteractEvent event){
        if(event.getItem() != null && TiboiseItem.getItemId(event.getItem()).equals(new CopperCompass().getId()) && event.getAction().isRightClick()){
            Player p = event.getPlayer();
            showPlayerMarkerListInventory(p);
        }
    }
    @EventHandler
    public static void cleanMapsOnPlayerCloseInventory(InventoryCloseEvent event){
        inventoryLink.remove(event.getInventory());
        inventoryTypeLink.remove(event.getInventory());
    }

    public static void showPlayerMarkerListInventory(Player p){
        Inventory showInv = Bukkit.createInventory(p,9*MARKER_INVENTORY_ROW_AMOUNT, Component.text(ChatColor.DARK_GRAY+MARKER_INVENTORY_NAME));
        List<PlayerCompassMarker> markers = new ArrayList<>(PlayerCompassMarker.getPlayerMarkers(p));
        markers.sort(Comparator.comparing(PlayerCompassMarker::getCreationDate));
        Location lastDeath = p.getLastDeathLocation();
        if(lastDeath != null){
            PlayerCompassMarker deathMarker = new PlayerCompassMarker("Last Death", lastDeath, PlayerCompassMarker.MarkerVisualIcon.DEATH);
            showInv.setItem(getSlotForRow(1,5), deathMarker.getDisplayItem(true,true,p.getInventory().getItemInMainHand().getItemMeta() instanceof CompassMeta cm && deathMarker.getDestination().equals(cm.getLodestone())));
        }
    
        int slot = 9;
        for (PlayerCompassMarker mk : markers) {
            int rowPosition = (slot % 9) + 1;
            if (rowPosition == 1) {
                slot++;
            } else if (rowPosition == 9) {
                slot += 2;
            }
            ItemStack display = mk.getDisplayItem(true, true,p.getInventory().getItemInMainHand().getItemMeta() instanceof CompassMeta cm && mk.getDestination().equals(cm.getLodestone()));
            showInv.setItem(slot, display);
            slot++;
            if (slot >= (9*(MARKER_INVENTORY_ROW_AMOUNT-1)) - 1) {
                break;
            }
        }
        // TOTEST
        ItemStack addMarkerButton = new GUIDisplayItem(MarkerOptionsButton.ADD_NEW.toString(),Material.RED_BANNER)
                .setName(Component.text(ChatColor.GOLD + "Add a new Marker").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .setLore(List.of(Component.text(ChatColor.YELLOW+">> Click to add a new Marker at your position <<")))
                .get();
        showInv.setItem(getSlotForRow(MARKER_INVENTORY_ROW_AMOUNT,5), addMarkerButton);
        
        registerMarkerInventory(showInv, null,MarkerInventoryType.LIST_DISPLAY);
        p.closeInventory();
        p.openInventory(showInv);
    }
    
    @EventHandler
    public static void onMarkerDisplayClick(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        
        if(inv.getHolder() instanceof Player p && inventoryTypeLink.containsKey(inv) && inventoryLink.containsKey(inv) && clicked != null && p.getInventory().getItemInMainHand().getItemMeta() instanceof CompassMeta cm){
            ItemStack compassitem = p.getInventory().getItemInMainHand();
            event.setCancelled(true);
            switch(inventoryTypeLink.get(inv)){
                case LIST_DISPLAY -> {
                    PlayerCompassMarker marker = PlayerCompassMarker.fromDisplay(clicked);
                    if(marker != null){
                        if(event.isLeftClick()){
                            if(marker.getDestination().equals(cm.getLodestone())){
                                cm.setLodestone(null);
                                compassitem.setItemMeta(cm);
                                showPlayerMarkerListInventory(p);
                            }else {
                                cm.setLodestoneTracked(false);
                                cm.setLodestone(marker.getDestination());
                                compassitem.setItemMeta(cm);
                                p.closeInventory();
                                p.showTitle(Title.title(Component.text(""), Component.text(ChatColor.RED+"Tracking "+ChatColor.BOLD+marker.getName()), Title.Times.times(Duration.ofMillis(100),Duration.ofMillis(2500),Duration.ofMillis(500))));
                                p.sendActionBar(Component.text(ChatColor.GOLD+""+(int)(p.getLocation().distance(marker.getDestination()))+" blocks away"));
                            }
                        } else {
                            showPlayerMarkerOptionsInventory(p, marker);
                        }
                    } else if(Objects.equals(GUIDisplayItem.getDisplayItemType(clicked), MarkerOptionsButton.ADD_NEW.toString())){
                        PlayerCompassMarker.addMarkerForPlayer(p, new PlayerCompassMarker("Marker",p.getLocation()));
                        showPlayerMarkerListInventory(p);
                    }
                }
                case OPTIONS -> {
                    MarkerOptionsButton role = null;
                    String type = GUIDisplayItem.getDisplayItemType(clicked);
                    if(type != null){
                        try{
                            role = MarkerOptionsButton.valueOf(type);
                        }catch (IllegalArgumentException ignored){}
                    } else {
                        return;
                    }
                    if(role != null){
                        PlayerCompassMarker marker = inventoryLink.get(inv);
                        switch(role)
                        {
                            case RENAME -> {
                                TextComponent msg = Component
                                        .text(ChatColor.GOLD+""+ChatColor.UNDERLINE+">> Click here to rename your Marker <<")
                                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/marker rename "+marker.getId().toString().substring(0,7)+" "));
                                p.sendMessage(msg);
                                p.closeInventory();
                                
                            }
                            case DELETE -> {
                                if(marker.getDestination().equals(cm.getLodestone())){
                                    cm.setLodestone(null);
                                    compassitem.setItemMeta(cm);
                                }
                                PlayerCompassMarker.removeMarkerForPlayer(p, marker.getId());
                                showPlayerMarkerListInventory(p);
                            }
                            case CHANGE_ICON -> {
                                showPlayerIconListInventory(p,marker);
                            }
                            case BACK -> {
                                showPlayerMarkerListInventory(p);
                            }
                        }
                    }
                }
                case ICONS -> {
                    String displayRaw = GUIDisplayItem.getDisplayItemType(clicked);
                    if(displayRaw != null && displayRaw.equalsIgnoreCase(MarkerOptionsButton.BACK.toString())){
                        PlayerCompassMarker marker = inventoryLink.get(inv);
                        if(marker != null){
                            showPlayerMarkerOptionsInventory(p,marker);
                        } else {
                            showPlayerMarkerListInventory(p);
                        }
                        return;
                    }
                    if(displayRaw != null && displayRaw.split("\\+").length >= 2){
                        String[] splitted = displayRaw.split("\\+");
                        String type = splitted[0];
                        String iconname = splitted[1];
                        if(type != null && type.equalsIgnoreCase(MarkerOptionsButton.SET_ICON.toString()) && iconname != null){
                            PlayerCompassMarker.MarkerVisualIcon icon = null;
                            try{
                                icon = PlayerCompassMarker.MarkerVisualIcon.valueOf(iconname);
                            } catch (IllegalArgumentException ignored){}
                            PlayerCompassMarker marker = inventoryLink.get(inv);
                            if(marker != null){
                                // normally this should properly edit the marker without having to redo it
                                marker.setIcon(icon);
                                showPlayerMarkerListInventory(p);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void showPlayerIconListInventory(Player p, PlayerCompassMarker marker){
        Inventory iconListGui = Bukkit.createInventory(p,9*MARKER_INVENTORY_ROW_AMOUNT, Component.text(ChatColor.DARK_GRAY+"Edit Marker Icon"));
        ItemStack usedIcon = marker.getDisplayItem(false,false);
        iconListGui.setItem(13, usedIcon);
        int slot = 18;
        for(PlayerCompassMarker.MarkerVisualIcon icon : PlayerCompassMarker.MarkerVisualIcon.values()){
            ItemStack iconDisplay = new GUIDisplayItem(MarkerOptionsButton.SET_ICON+"+"+icon.toString(),PlayerCompassMarker.DEFAULT_MARKER_MATERIAL)
                    .setName(Component.text(ChatColor.GOLD+icon.getDisplayName()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .setLore(List.of(Component.text(ChatColor.YELLOW+">> Click here to select this icon for your marker <<")))
                    .setIcon(icon.toString().toLowerCase(Locale.ROOT))
                    .get();
            
            int rowPosition = (slot % 9) + 1;
            if (rowPosition == 1) {
                slot++;
            } else if (rowPosition == 9) {
                slot += 2;
            }

            iconListGui.setItem(slot, iconDisplay);

            slot++;
            if (slot >= (9 * (MARKER_INVENTORY_ROW_AMOUNT - 1)) - 1) {
                break;
            }
        }
    
        iconListGui.setItem(getSlotForRow(MARKER_INVENTORY_ROW_AMOUNT,1), backButtonFactory.get());
    
        registerMarkerInventory(iconListGui,marker,MarkerInventoryType.ICONS);
        p.closeInventory();
        p.openInventory(iconListGui);
    }

    // TODO : methods to set and retrieve icon data for icon selection buttons
    
    @Override
    public ItemStack get() {
        ItemStack otp = super.get();
        ItemMeta meta = otp.getItemMeta().clone();
        meta.setCustomModelData(1);
        otp.setItemMeta(meta);
        return otp;
    }
    
    @Override
    public @Nullable Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getItemIdKey(),this.get());
        recipe.shape("CRC","RBR","CRC").setIngredient('C', Material.COPPER_INGOT).setIngredient('R',Material.REDSTONE).setIngredient('B',new ItemStack(Material.COMPASS));
        return recipe;
    }

    private static void showPlayerMarkerOptionsInventory(Player p, PlayerCompassMarker marker){
        Inventory optionsgui = Bukkit.createInventory(p,2*9,Component.text(ChatColor.DARK_GRAY+"Modifying Marker : "+marker.getName()));
        optionsgui.setItem(4,marker.getDisplayItem(false,false));
        
        ItemStack renameButton = new GUIDisplayItem(MarkerOptionsButton.RENAME.toString(),Material.NAME_TAG)
                .setName(Component.text(ChatColor.GOLD + "Rename Marker").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .setLore(List.of(Component.text(ChatColor.YELLOW+">> Click to rename the marker <<")))
                .get();
        ItemStack iconButton = new GUIDisplayItem(MarkerOptionsButton.CHANGE_ICON.toString(),Material.GLOBE_BANNER_PATTERN)
                .setName(Component.text(ChatColor.GOLD + "Change Marker Icon").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .setLore(List.of(Component.text(ChatColor.YELLOW+">> Click to change the icon of the marker <<")))
                .get();
        ItemStack deleteButton = new GUIDisplayItem(MarkerOptionsButton.DELETE.toString(),Material.BARRIER)
                .setName(Component.text(ChatColor.GOLD + "Delete Marker").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .setLore(List.of(Component.text(ChatColor.YELLOW+">> Click to delete the marker <<")))
                .get();
        ItemStack backButton = backButtonFactory.get();

        optionsgui.setItem(9, backButton);
        optionsgui.setItem(12, renameButton);
        optionsgui.setItem(13, iconButton);
        optionsgui.setItem(14, deleteButton);
        // 00 01 02 03 04 05 06 07 08
        // 09 10 11 12 13 14 15 16 17

        // REGISTER INVENTORY
        // {}


        // TODO : When creating the DisplayItem class we'll have to create a logging system for inventories following
        //  Map<String, Inventory> where String is a custom identifier provided by the registerer and Inventory is the
        //  inventory to be tracked itself.

        registerMarkerInventory(optionsgui,marker,MarkerInventoryType.OPTIONS);
        p.closeInventory();
        p.openInventory(optionsgui);
    }

    public static void registerMarkerInventory(Inventory inv, PlayerCompassMarker marker, MarkerInventoryType inventoryType){
        inventoryLink.put(inv,marker);
        inventoryTypeLink.put(inv,inventoryType);
    }
    public static @Nullable PlayerCompassMarker getInventoryMarker(Inventory inv){
        return inventoryLink.get(inv);
    }
    public static @Nullable MarkerInventoryType getInventoryType(Inventory inv){
        return inventoryTypeLink.get(inv);
    }
    
    /**
     *
     * @param row The row of which you want to get the place (starting from 1)
     * @param place The slot to get the value (starting from 1, left)
     * @return The absolute value of the slot (used in Bukkit Inventories to refer to inventory slots)
     */
    private static int getSlotForRow(int row, int place){
        return (9*(row-1))-1+place;
    }
}
