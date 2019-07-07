package io.github.archemedes.betterbooks;

import net.lordofthecraft.omniscience.api.entry.OEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

class BlockLoggingTool {

	private static BlockLoggingTool singleton = null;
    
	public static BlockLoggingTool get() {
		if(singleton == null) singleton = new BlockLoggingTool();
		return singleton;
	}
	
	private BlockLoggingTool() {}
	
	public void handleShelfOpen(InventoryOpenEvent event ) {
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Omniscience")) {
			return;
		}
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BookShelf) {
	        OEntry.create().source(event.getPlayer()).opened(holder).save();
        }
	}
	
	public void handleShelfClose(InventoryCloseEvent event) {
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Omniscience")) {
			return;
		}
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BookShelf ) {
	        OEntry.create().source(event.getPlayer()).closed(event.getInventory().getHolder()).save();
        }
	}
}
