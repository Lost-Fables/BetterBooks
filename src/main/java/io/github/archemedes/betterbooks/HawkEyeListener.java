package io.github.archemedes.betterbooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class HawkEyeListener implements Listener {

    // Log when books enter or leave bookshelves
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
    	HawkEyeTool.get().handleShelfClose(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
    	HawkEyeTool.get().handleShelfOpen(event);
    }

}
