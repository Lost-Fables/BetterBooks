package io.github.archemedes.betterbooks;

import net.lordofthecraft.omniscience.api.data.InventoryTransaction;
import net.lordofthecraft.omniscience.api.entry.OEntry;
import net.lordofthecraft.omniscience.api.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
	        OEntry.create().source(event.getPlayer()).opened(((BookShelf) holder).getLocation(), "bookshelf").save();
        }
	}
	
	public void handleShelfClose(InventoryCloseEvent event) {
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Omniscience")) {
			return;
		}
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BookShelf ) {
	        OEntry.create().source(event.getPlayer()).closed(((BookShelf) holder).getLocation(), "bookshelf").save();
        }
	}

	public void handleInventoryClick(InventoryClickEvent e) {
		// This refactor goes out to tofuus who took the bait hook line and sinker
		List<InventoryTransaction<ItemStack>> transactions = InventoryUtil.identifyTransactions(e);
		for (InventoryTransaction<ItemStack> transaction : transactions) {
			if (transaction.getType() == InventoryTransaction.ActionType.CLONE) {
				OEntry.create().player(e.getWhoClicked()).cloned(transaction.getDiff()).save();
			} else {
				InventoryHolder holder = transaction.getHolder();
				if (holder instanceof BookShelf) {
					Location location = ((BookShelf) holder).getLocation();

					switch (transaction.getType()) {
						case WITHDRAW:
							OEntry.create().player(e.getWhoClicked()).withdrew(transaction, location, "bookshelf").save();
							break;
						case DEPOSIT:
							OEntry.create().player(e.getWhoClicked()).deposited(transaction, location, "bookshelf").save();
							break;
						case CLONE:
							break;
					}
				}
			}
		}
	}
}
