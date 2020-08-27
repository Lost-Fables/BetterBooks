package io.github.archemedes.betterbooks;

import co.lotc.core.bukkit.util.InventoryUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class BookShelf implements InventoryHolder {
    public static final String BOOKSHELF_NAME = "Bookshelf";
    private static final Map<Location, BookShelf> shelves = Maps.newHashMap();
    private final List<String> viewers = Lists.newArrayList();
    private final Inventory inv;
    private final Location loc;
    private boolean changed = false;

    private BookShelf(Location where) {
        this.loc = where;
        this.inv = Bukkit.createInventory(this, 9, "Bookshelf");
    }

    public static BookShelf getBookshelf(Block b) {
        Location l = b.getLocation();

        if (b.getType() != Material.BOOKSHELF) {
            return null;
        }
        if (shelves.containsKey(l)) {
            return shelves.get(l);
        }
        BookShelf res = new BookShelf(l);
        shelves.put(l, res);
        return res;
    }

    public static boolean hasBookShelf(Block b) {
        return shelves.containsKey(b.getLocation()) && b.getType() == Material.BOOKSHELF;
    }

    public static Map<Location, BookShelf> getAllShelves() {
        return Collections.unmodifiableMap(shelves);
    }
    
    public ItemStack[] cleanupForbiddenItems() {
    	ItemStack[] cs = inv.getContents();
    	List<ItemStack> forbidden = new ArrayList<>();
    	for(int i = 0; i < cs.length; i++) {
    		ItemStack is = cs[i];
    		if(is == null) continue;
    		Material m = is.getType();
    		if(m != Material.WRITTEN_BOOK && m != Material.WRITABLE_BOOK && m!= Material.BOOK
    				&& m != Material.PAPER && m != Material.MAP) {
    			cs[i] = null;
    			forbidden.add(is);
    		}
    	}
    	
    	if(!forbidden.isEmpty()) inv.setContents(cs);
    	ItemStack[] result = new ItemStack[forbidden.size()];
    	return forbidden.toArray(result);
    }

    public void addViewer(Player p) {
        this.viewers.add(p.getName());
    }

    public void removeViewer(Player p) {
        this.viewers.remove(p.getName());
    }

    public boolean isViewer(Player p) {
        return this.viewers.contains(p.getName());
    }

    @Override
	public Inventory getInventory() {
        return this.inv;
    }

    public Location getLocation() {
        return this.loc.clone();
    }

    public void setChanged() {
        this.changed = true;
    }

    public void close() {
        boolean empty = isInventoryEmpty(this.inv);

        if (empty) {
            shelves.remove(this.loc);
            BetterBooks.db.deleteShelf(loc);
            return;
        }

        if (this.changed) {
            this.changed = false;
            BetterBooks.db.addOrUpdate(this);
        } else if (getLocation().getBlock().getType() != Material.BOOKSHELF) {
            BetterBooks.db.deleteShelf(loc);
        }
    }
    
    public void remove(boolean drops) {
        boolean empty = true;

        shelves.remove(this.loc);

        List<HumanEntity> list = new ArrayList<>(this.inv.getViewers());
        list.forEach(HumanEntity::closeInventory);

        for (ItemStack is : this.inv) {
            if (is != null) {
                empty = false;
                if (!drops) break;
                this.loc.getWorld().dropItemNaturally(this.loc, is);
            }
        }

        if (!empty) {
            BetterBooks.db.deleteShelf(loc);
        }
    }

    private boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item.getType().equals(Material.AIR)) {
                return false;
            }
        }
        return true;
    }

    public void loadFromString(String inv) {
        if (inv != null) {
            this.inv.setContents(InventoryUtil.deserializeItemsToArray(inv));
        }
    }
}