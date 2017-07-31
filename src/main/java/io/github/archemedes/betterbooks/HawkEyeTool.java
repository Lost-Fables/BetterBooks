package io.github.archemedes.betterbooks;

import java.util.HashMap;
import java.util.List;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import uk.co.oliwali.HawkEye.DataType;
import uk.co.oliwali.HawkEye.HawkEye;
import uk.co.oliwali.HawkEye.entry.DataEntry;
import uk.co.oliwali.HawkEye.entry.containerentries.ContainerEntry;
import uk.co.oliwali.HawkEye.entry.containerentries.ContainerExtract;
import uk.co.oliwali.HawkEye.entry.containerentries.ContainerInsert;
import uk.co.oliwali.HawkEye.util.InventoryUtil;

class HawkEyeTool {

	private static HawkEyeTool singleton = null;
	
    private HashMap<String, List<ItemStack>> invTransactions = new HashMap<>();
    
	public static HawkEyeTool get() {
		if(singleton == null) singleton = new HawkEyeTool();
		return singleton;
	}
	
	private HawkEyeTool() {}
	
	public void handleShelfOpen(InventoryOpenEvent event ) {
        String player = event.getPlayer().getName();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BookShelf) {
            BookShelf bookShelf = (BookShelf) holder;

            invTransactions.put(player, InventoryUtil.compressInventory(holder.getInventory().getContents()));
            HawkEye.getDbmanager().getConsumer().addEntry(new DataEntry(player, DataType.OPEN_CONTAINER, bookShelf.getLocation(), "Bookshelf"));
        }
	}
	
	public void handleShelfClose(InventoryCloseEvent event) {
        String player = event.getPlayer().getName();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BookShelf && invTransactions.containsKey(player)) {
            List<ItemStack> oldInv = invTransactions.get(player);
            BookShelf bookShelf = (BookShelf) holder;

            if (oldInv != null) {
                invTransactions.remove(player);

                List<ItemStack>[] dif = InventoryUtil.getDifference(oldInv, InventoryUtil.compressInventory(holder.getInventory().getContents()));

                if (dif[0].size() > 0 && DataType.CONTAINER_EXTRACT.isLogged()) {
                    for (String str : InventoryUtil.serializeInventory(ContainerEntry.getSerializer(), dif[0])) {
                        HawkEye.getDbmanager().getConsumer().addEntry(new ContainerExtract(player, DataType.CONTAINER_EXTRACT, bookShelf.getLocation(), str));
                    }
                }

                if (dif[1].size() > 0 && DataType.CONTAINER_INSERT.isLogged()) {
                    for (String str : InventoryUtil.serializeInventory(ContainerEntry.getSerializer(), dif[1])) {
                        HawkEye.getDbmanager().getConsumer().addEntry(new ContainerInsert(player, DataType.CONTAINER_INSERT, bookShelf.getLocation(), str));
                    }
                }

            }
        }
	}
	
}
