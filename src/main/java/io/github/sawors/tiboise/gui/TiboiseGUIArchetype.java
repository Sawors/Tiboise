package io.github.sawors.tiboise.gui;

import net.kyori.adventure.text.Component;

import java.util.Map;

public class TiboiseGUIArchetype {

    private int rows = 6;
    private Component name;
    private Map<GUIDisplayItem, Integer> inventoryButtonsMap;
    // GUIDisplayItem : is for the button directly
    // Integer : is for the slot the button should be displayed in (starting from 0)

    protected TiboiseGUIArchetype(TiboiseGUI base){
        this.rows = base.getRowAmount();
        this.name = base.getName();
        this.inventoryButtonsMap = Map.copyOf(base.getButtonMap());
    }

    public TiboiseGUI buildGUI(){
        TiboiseGUI gui = new TiboiseGUI();
        for(Map.Entry<GUIDisplayItem, Integer> entry : inventoryButtonsMap.entrySet()){
            gui.addButton(entry.getKey(),entry.getValue());
        }
        gui.setName(name);
        gui.setSize(rows);

        return gui;
    }


}
