package com.flyaway.xpget;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class XpGetListener implements Listener {

    public XpGetListener() {
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof XpGetGUI xpGetGUI) {
            xpGetGUI.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof XpGetGUI) {
            event.setCancelled(true);
        }
    }
}
