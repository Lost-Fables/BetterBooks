package io.github.archemedes.betterbooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.save.SaveHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BookShelf
        implements InventoryHolder {
    public static final String BOOKSHELF_NAME = "Bookshelf";
    private static final Map<Location, BookShelf> shelves = Maps.newHashMap();
    private static SaveHandler buffer;
    private final List<String> viewers = Lists.newArrayList();
    private final Inventory inv;
    private final Location key;
    private boolean changed = false;

    private BookShelf(Location where) {
        this.key = where;
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
        return shelves.containsKey(b.getLocation());
    }

    public static Map<Location, BookShelf> getAllShelves() {
        return Collections.unmodifiableMap(shelves);
    }

    static void init(BetterBooks plugin) {
        buffer = SaveHandler.getInstance();
        IArcheCore control = ArcheCore.getControls();
        SQLHandler handler = control.getSQLHandler();

        handler.execute("CREATE TABLE IF NOT EXISTS books (world TEXT, x INT, y INT, z INT, slot INT, mat TEXT, count INT, damage INT, title TEXT, author TEXT, lore TEXT, pages TEXT);");
        ResultSet res = null;
        try {
            res = handler.query("SELECT * FROM books;");
            populateBookshelves(res);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(res);
        }
    }

    static void populateBookshelves(ResultSet res) throws SQLException {
        while (res.next()) {
            String world = res.getString(1);
            World w = Bukkit.getWorld(world);
            if (w != null) {
                int x = res.getInt(2);
                int y = res.getInt(3);
                int z = res.getInt(4);

                Block b = w.getBlockAt(x, y, z);
                BookShelf s = getBookshelf(b);
                if (s != null) {
                    int slot = res.getInt(5);
                    ItemStack book = SQLSerializer.deserialize(res);

                    s.getInventory().setItem(slot, book);
                }
            }
        }
    }

    private static void close(ResultSet res) {
        if (res != null) try {
            res.close();
            res.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        return this.key.clone();
    }

    public void setChanged() {
        this.changed = true;
    }

    public void close() {
        boolean empty = isInventoryEmpty(this.inv);

        if (this.changed) {
            this.changed = false;

            ItemStack[] contents = empty ? null : this.inv.getContents();
            ClearShelfTask task = new ClearShelfTask(contents, getLocation());
            buffer.put(task);
        } else if (getLocation().getBlock().getType() != Material.BOOKSHELF) {
            ClearShelfTask task = new ClearShelfTask(null, getLocation());
            buffer.put(task);
        }

        if (empty) shelves.remove(this.key);
    }

    public void remove(boolean drops) {
        boolean empty = true;

        shelves.remove(this.key);

        List<HumanEntity> list = new ArrayList<>(this.inv.getViewers());
        list.forEach(HumanEntity::closeInventory);

        for (ItemStack is : this.inv) {
            if (is != null) {
                empty = false;
                if (!drops) break;
                this.key.getWorld().dropItemNaturally(this.key, is);
            }

        }

        if (!empty) {
            ClearShelfTask task = new ClearShelfTask(null, getLocation());
            buffer.put(task);
        }
    }

    private boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }
}