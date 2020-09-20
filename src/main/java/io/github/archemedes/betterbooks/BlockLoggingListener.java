package io.github.archemedes.betterbooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class BlockLoggingListener implements Listener {

    // Log when books enter or leave bookshelves
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof BookShelf) {
			BlockLoggingTool.get().handleShelfClose(event);
		}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.getInventory().getHolder() instanceof BookShelf) {
			BlockLoggingTool.get().handleShelfOpen(event);
		}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
    	if (event.getInventory().getHolder() instanceof BookShelf ||
			(event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof BookShelf)) {
			BlockLoggingTool.get().handleInventoryClick(event);
		}
    }

}
