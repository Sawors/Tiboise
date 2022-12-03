package io.github.sawors.tiboise.gui;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class TiboiseGUI implements Listener {

    // static fields referring to the global storage of inventories
    public static Map<Inventory, String> inventoryReferenceMap = new HashMap<>();
    private static Map<Inventory, String> inventoryTypeMap = new HashMap<>();
    public static Map<String, String> linkMap = new HashMap<>();
    // fields referring to this special inventory (instanced)
    private Inventory displayInventory;
    private int rows = 6;
    private Component name = Component.text(this.getClass().getName());
    private final String reference;
    private String type = "undefined";
    private InventoryHolder holder = null;

    private Map<GUIDisplayItem, Integer> inventoryButtonsMap = new HashMap<>();
    // GUIDisplayItem : is for the button directly
    // Integer : is for the slot the button should be displayed in (starting from 0)

    public TiboiseGUI(){
        this.reference = RandomStringUtils.randomAlphanumeric(8);
    }

    @EventHandler
    public static void cleanMapsOnPlayerCloseInventory(InventoryCloseEvent event){
        inventoryReferenceMap.remove(event.getInventory());
        inventoryTypeMap.remove(event.getInventory());
    }

    public String getReference(){
        return reference;
    }
    
    public enum SortingType{
        ALPHABETICAL, DATE, VALUE, DISTANCE
    }

    public TiboiseGUI setHolder(InventoryHolder holder){
        this.holder = holder;
        return this;
    }

    public int getRowAmount() {
        return this.rows;
    }

    public Component getName(){
        return this.name;
    }

    /**
     *
     * @return the MUTABLE version of the button map. The main difference with TiboiseGUIArchetype is that the Archetype map is immutable
     */
    protected Map<GUIDisplayItem, Integer> getButtonMap(){
        return this.inventoryButtonsMap;
    }

    public void addButton(GUIDisplayItem button, int slot){
        inventoryButtonsMap.put(button,slot);
    }

    public void setName(Component name){
        this.name = name;
    }

    public void setSize(int rows){
        this.rows = rows;
    }


    public TiboiseGUI build(){
        Inventory tempInventory = Bukkit.createInventory(holder,rows);
        for(Map.Entry<GUIDisplayItem, Integer> entry : inventoryButtonsMap.entrySet()){
            int slot = entry.getValue();
            ItemStack item = entry.getKey().get();
            if(slot < rows*9){
                tempInventory.setItem(slot,item);
            }
        }

        this.displayInventory = tempInventory;
        inventoryReferenceMap.put(displayInventory, reference);
        inventoryTypeMap.put(displayInventory,type);
        return this;
    }

    public Inventory getDisplayInventory(){

        return displayInventory;
    }

    /**
     *
     * @param row The row of which you want to get the place (from 1 to 6)
     * @param place The slot to get the value (starting from the left, from 1 to 9)
     * @return The absolute value of the slot (used in Bukkit Inventories to refer to inventory slots)
     */
    public static int getSlotForRow(int row, int place){
        return (9*(row-1))-1+place;
    }
    
}
