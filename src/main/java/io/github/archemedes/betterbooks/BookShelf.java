package io.github.archemedes.betterbooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.archemedes.betterbooks.io.BookRow;
import io.github.archemedes.betterbooks.io.DelBookRow;
import io.github.archemedes.betterbooks.io.UpdateBookRow;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class BookShelf implements InventoryHolder {
    public static final String BOOKSHELF_NAME = "Bookshelf";
    private static final Map<Location, BookShelf> shelves = Maps.newHashMap();
    //private static SaveHandler buffer;
    private static IConsumer consumer;
    private final List<String> viewers = Lists.newArrayList();
    private final Inventory inv;
    private final Location key;
    private boolean changed = false;
    private boolean hasRow = false;

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
        return shelves.containsKey(b.getLocation()) && b.getType() == Material.BOOKSHELF;
    }

    public static Map<Location, BookShelf> getAllShelves() {
        return Collections.unmodifiableMap(shelves);
    }

    static void init(BetterBooks plugin) {
        //buffer = SaveHandler.getInstance();
        IArcheCore control = ArcheCore.getControls();
        consumer = control.getConsumer();
        SQLHandler handler = control.getSQLHandler();

        handler.execute("CREATE TABLE IF NOT EXISTS books (world CHAR(36), x INT, y INT, z INT, inv TEXT, PRIMARY KEY (world,x,y,z)) " + (ArcheCore.usingSQLite() ? ";" : "ENGINE=InnoDB DEFAULT CHARSET=utf8;"));
        ResultSet res = null;
        try {
            res = handler.query("SELECT world,x,y,z,inv FROM books;");
            populateBookshelves(res);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(res);
        }
    }

    static void populateBookshelves(ResultSet res) throws SQLException {
        while (res.next()) {
            String world = res.getString("world");
            World w = Bukkit.getWorld(UUID.fromString(world));
            if (w != null) {
                int x = res.getInt("x");
                int y = res.getInt("y");
                int z = res.getInt("z");

                Block b = w.getBlockAt(x, y, z);
                BookShelf s = getBookshelf(b);
                if (s != null) {
                    s.loadFromString(res.getString("inv"));
                    s.hasRow = true;
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
    
    public ItemStack[] cleanupForbiddenItems() {
    	ItemStack[] cs = inv.getContents();
    	List<ItemStack> forbidden = new ArrayList<>();
    	for(int i = 0; i < cs.length; i++) {
    		ItemStack is = cs[i];
    		if(is == null) continue;
    		Material m = is.getType();
    		if(m != Material.WRITTEN_BOOK && m != Material.BOOK_AND_QUILL && m!= Material.BOOK
    				&& m != Material.PAPER && m != Material.EMPTY_MAP) {
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
        return this.key.clone();
    }

    public void setChanged() {
        this.changed = true;
    }

    public void close() {
        boolean empty = isInventoryEmpty(this.inv);

        if (empty) {
            shelves.remove(this.key);
            consumer.queueRow(new DelBookRow(key));
            return;
        }

        if (this.changed) {
            this.changed = false;
            if (!hasRow) {
                consumer.queueRow(new BookRow(this));
                hasRow = true;
            } else {
                consumer.queueRow(new UpdateBookRow(key.clone(), saveToString()));
            }
        } else if (getLocation().getBlock().getType() != Material.BOOKSHELF) {
            consumer.queueRow(new DelBookRow(key.clone()));
        }
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
            consumer.queueRow(new DelBookRow(key.clone()));
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

    @SuppressWarnings("unchecked")
    private void loadFromString(String inv) {
        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] contents;
        try {
            if (inv != null) {
                config.loadFromString(inv);
                List<ItemStack> result = config.getList("c").stream()
                        .map(ent -> (Map<String, Object>) ent)
                        .map(ent -> ent == null ? null : ItemStack.deserialize(ent))
                        .collect(Collectors.toList());
                contents = result.toArray(new ItemStack[result.size()]);
            } else {
                contents = new ItemStack[9];
            }
        } catch (InvalidConfigurationException e) {
            contents = new ItemStack[9];
            e.printStackTrace();
        }
        this.inv.setContents(contents);
    }

    public String saveToString() {
        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] contents = inv.getContents();
        List<Map<String, Object>> contentslist = Lists.newArrayList();
        for (ItemStack i : contents) {
            if (i == null) contentslist.add(null);
            else contentslist.add(i.serialize());
        }
        config.set("c", contentslist);
        return config.saveToString();
    }
}