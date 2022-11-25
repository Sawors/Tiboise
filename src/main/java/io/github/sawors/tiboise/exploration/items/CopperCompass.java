package io.github.sawors.tiboise.exploration.items;

import io.github.sawors.tiboise.Main;
import io.github.sawors.tiboise.exploration.PlayerCompassMarker;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CopperCompass extends TiboiseItem implements Listener {
    
    private final static String MARKER_INVENTORY_NAME = "Markers";
    private final static int MARKER_INVENTORY_ROW_AMOUNT = 6;
    // TODO : move this to the future DisplayItem class
    private static Map<ItemStack, MarkerOptionsButton> buttonLink = new HashMap<>();
    private static Map<Inventory, PlayerCompassMarker> inventoryLink = new HashMap<>();
    private static Map<Inventory, MarkerInventoryType> inventoryTypeLink = new HashMap<>();


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

    public static void showPlayerMarkerListInventory(Player p){
        Inventory showInv = Bukkit.createInventory(p,9*MARKER_INVENTORY_ROW_AMOUNT, Component.text(ChatColor.DARK_GRAY+MARKER_INVENTORY_NAME));
        List<PlayerCompassMarker> markers = List.copyOf(PlayerCompassMarker.getPlayerMarkers(p));

        Location lastDeath = p.getLastDeathLocation();
        // TOTEST
        if(lastDeath != null){
            PlayerCompassMarker deathMarker = new PlayerCompassMarker("Last Death", lastDeath, PlayerCompassMarker.MarkerVisualIcons.DEATH);
            showInv.setItem(4, deathMarker.getDisplayItem(true,true));
        }

        if(markers != null){
            int slot = 9;
            for (PlayerCompassMarker mk : markers) {
                int rowPosition = (slot % 9) + 1;
                if (rowPosition == 1) {
                    slot++;
                } else if (rowPosition == 9) {
                    slot += 2;
                }
                ItemStack display = mk.getDisplayItem(true, true);
                showInv.setItem(slot, display);
                slot++;
                if (slot >= (9 * (MARKER_INVENTORY_ROW_AMOUNT - 1)) - 1) {
                    break;
                }
            }
            // TOTEST
            ItemStack addMarkerButton = new ItemStack(Material.RED_BANNER);
            addMarkerButton.editMeta(m -> {
                m.displayName(Component.text(ChatColor.GOLD+"Add a new Marker"));
                m.lore(List.of(Component.text(ChatColor.YELLOW+">> Click to create a new marker at your position <<")));
            });
            showInv.setItem((9*MARKER_INVENTORY_ROW_AMOUNT)+4, addMarkerButton);
            registerMarkerOptionButton(addMarkerButton, MarkerOptionsButton.ADD_NEW);
        }
        registerMarkerInventory(showInv, null,MarkerInventoryType.LIST_DISPLAY);
        p.closeInventory();
        p.openInventory(showInv);
    }
    
    @EventHandler
    public static void onMarkerDisplayClick(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        if(inv.getHolder() instanceof Player p && inventoryTypeLink.containsKey(inv) && inventoryLink.containsKey(inv)){
            ItemStack clicked = p.getInventory().getItemInMainHand();
            event.setCancelled(true);
            switch(inventoryTypeLink.get(inv)){
                case LIST_DISPLAY -> {
                    PlayerCompassMarker marker = PlayerCompassMarker.fromDisplay(event.getCurrentItem());
                    if(marker != null){
                        if(event.isLeftClick()){
                            if(clicked.getItemMeta() instanceof CompassMeta cm){
                                cm.setLodestoneTracked(true);
                                cm.setLodestone(marker.getDestination().add(0,(-marker.getDestination().getY())-64,0));
                                clicked.setItemMeta(cm);
                                p.closeInventory();
                            }
                        } else {
                            showPlayerMarkerOptionsInventory(p, marker);
                        }
                    }
                }
                case OPTIONS -> {
                    MarkerOptionsButton role = getButtonRole(clicked);
                    if(role != null){
                        switch(role)
                        {
                            case RENAME -> {

                            }
                            case DELETE -> {

                            }
                            case ADD_NEW -> {

                            }
                            case CHANGE_ICON -> {

                            }
                            case BACK -> {
                                showPlayerMarkerListInventory(p);
                            }
                        }
                    }
                }
                case ICONS -> {

                }
            }
        }
    }

    public static void showPlayerIconListInventory(Player p, PlayerCompassMarker marker){
        Inventory iconList = Bukkit.createInventory(p,9*MARKER_INVENTORY_ROW_AMOUNT, Component.text(ChatColor.DARK_GRAY+"Edit Marker Icon"));
        ItemStack usedIcon = marker.getDisplayItem(false,false);
        iconList.setItem(13, usedIcon);
        int slot = 18;
        for(PlayerCompassMarker.MarkerVisualIcons icon : PlayerCompassMarker.MarkerVisualIcons.values()){
            ItemStack iconDisplay = new ItemStack(PlayerCompassMarker.DEFAULT_MARKER_MATERIAL);
            iconDisplay.editMeta(m -> {
                String name = icon.toString();
                name = Main.getUpperCamelCase(name.replaceAll("_"," "));
                m.displayName(Component.text(ChatColor.WHITE+name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                m.getPersistentDataContainer().set(PlayerCompassMarker.getIconTypeKey(), PersistentDataType.STRING, icon.toString().toLowerCase(Locale.ROOT));
                m.setCustomModelData(icon.getCustomModelValue());
            });
            int rowPosition = (slot % 9) + 1;
            if (rowPosition == 1) {
                slot++;
            } else if (rowPosition == 9) {
                slot += 2;
            }

            iconList.setItem(slot, iconDisplay);

            slot++;
            if (slot >= (9 * (MARKER_INVENTORY_ROW_AMOUNT - 1)) - 1) {
                break;
            }
        }

        p.closeInventory();
        p.openInventory(iconList);
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
        Inventory options = Bukkit.createInventory(p,2*9,Component.text(ChatColor.DARK_GRAY+"Modifying Marker : "+marker.getName()));
        options.setItem(4,marker.getDisplayItem(false,false));
        ItemStack renameButton = new ItemStack(Material.NAME_TAG);
        renameButton.editMeta(m -> {
            m.displayName(Component.text(ChatColor.GOLD + "Rename Marker").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            m.lore(List.of(Component.text(ChatColor.YELLOW+"Click to rename the marker")));
        });
        ItemStack iconButton = new ItemStack(Material.GLOBE_BANNER_PATTERN);
        iconButton.editMeta(m -> {
            m.displayName(Component.text(ChatColor.GOLD + "Change Marker Icon").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            m.lore(List.of(Component.text(ChatColor.YELLOW+"Click to change the icon of the marker")));
        });
        ItemStack deleteButton = new ItemStack(Material.BARRIER);
        deleteButton.editMeta(m -> {
            m.displayName(Component.text(ChatColor.GOLD + "Delete Marker").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            m.lore(List.of(Component.text(ChatColor.YELLOW+"Click to delete the marker")));
        });
        ItemStack backButton = new ItemStack(Material.INK_SAC);
        backButton.editMeta(m -> {
            m.displayName(Component.text(ChatColor.GOLD + "← Go Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            m.lore(List.of(Component.text(ChatColor.YELLOW+"Click go back to the previous menu")));
        });

        options.setItem(9, backButton);
        options.setItem(12, renameButton);
        options.setItem(13, iconButton);
        options.setItem(14, deleteButton);
        registerMarkerOptionButton(backButton, MarkerOptionsButton.BACK);
        registerMarkerOptionButton(renameButton, MarkerOptionsButton.RENAME);
        registerMarkerOptionButton(iconButton, MarkerOptionsButton.CHANGE_ICON);
        registerMarkerOptionButton(deleteButton,MarkerOptionsButton.DELETE);
        // 00 01 02 03 04 05 06 07 08
        // 09 10 11 12 13 14 15 16 17

        // REGISTER INVENTORY
        // {}


        // TODO : When creating the DisplayItem class we'll have to create a logging system for inventories following
        //  Map<String, Inventory> where String is a custom identifier provided by the registerer and Inventory is the
        //  inventory to be tracked itself.

        registerMarkerInventory(options,marker,MarkerInventoryType.OPTIONS);
        p.closeInventory();
        p.openInventory(options);
    }

    public static void registerMarkerInventory(Inventory inv, PlayerCompassMarker marker, MarkerInventoryType inventoryType){
        inventoryLink.put(inv,marker);
        inventoryTypeLink.put(inv,inventoryType);
    }
    public static void registerMarkerOptionButton(ItemStack button, MarkerOptionsButton role){
        buttonLink.put(button,role);
    }
    public static @Nullable MarkerOptionsButton getButtonRole(ItemStack button){
        return buttonLink.get(button);
    }
    public static @Nullable PlayerCompassMarker getInventoryMarker(Inventory inv){
        return inventoryLink.get(inv);
    }
    public static @Nullable MarkerInventoryType getInventoryType(Inventory inv){
        return inventoryTypeLink.get(inv);
    }
}
