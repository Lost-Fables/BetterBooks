package io.github.archemedes.betterbooks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.collect.Maps;

public class BookSaveListener implements Listener {
    private final BetterBooks plugin;
    HashMap<String, OpenBook> readers = new HashMap<>();



    public BookSaveListener(BetterBooks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block b = event.getClickedBlock();
        Player p = event.getPlayer();

        if (p.getEquipment().getItemInMainHand().getType() == Material.BOOKSHELF || (p.isSneaking() && event.isBlockInHand())) {
            return;
        }

        Action a = event.getAction();
        if (a == Action.RIGHT_CLICK_BLOCK && b.getType() == Material.BOOKSHELF &&
                (event.getBlockFace() != BlockFace.UP) && (event.getBlockFace() != BlockFace.DOWN) ) {
        	
        	if(this.readers.containsKey(p.getName())) {
        		p.sendMessage(ChatColor.GRAY+ "You put down the book you were reading");
        		this.readers.remove(p.getName()).cancelTask();
        	}
        	
            if (p.getGameMode() == GameMode.CREATIVE) {
                p.sendMessage(ChatColor.RED + "You may not open bookshelves in creative mode.");
                event.setCancelled(true);
                return;
            } else if (event.isCancelled()) {
                if (BookShelf.hasBookShelf(b)) {
                    p.sendMessage(ChatColor.RED + "You may only read through the contents of this bookshelf.");
                    BookShelf shelf = BookShelf.getBookshelf(b);
                    shelf.addViewer(p);
                    p.openInventory(shelf.getInventory());
                } else {
                    p.sendMessage(ChatColor.RED + "You may not access this bookshelf at present.");
                }
            } else {
                BookShelf shelf = BookShelf.getBookshelf(b);
                Inventory inv = shelf.getInventory();
                System.out.println(inv.getViewers());
                if (shelf.getInventory().getViewers().size() == 0) {
                    changetracker.put(shelf.getLocation(), inv.getContents());
                }
                p.openInventory(inv);
            }
        }
    }

    private HashMap<Location, ItemStack[]> changetracker = Maps.newHashMap();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if ((inv.getHolder() instanceof BookShelf)) { event.setCancelled(true); }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof BookShelf)) {
            return;
        }
        BookShelf shelf = (BookShelf) inv.getHolder();
        final Player p = (Player) event.getWhoClicked();
        InventoryAction a = event.getAction();

        Material type = null;
        if (shelf.isViewer(p)) {
            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, p::closeInventory);
            if ((a == InventoryAction.PICKUP_ALL || a == InventoryAction.PICKUP_HALF || a == InventoryAction.PICKUP_SOME || a == InventoryAction.PICKUP_ONE) 
            		&& ((event.getCurrentItem().getType() == Material.WRITTEN_BOOK) || (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL))) {
                BookMeta meta = (BookMeta) event.getCurrentItem().getItemMeta();
                String title = meta.getTitle() == null ? "Unknown" : ChatColor.stripColor(meta.getTitle());
                String auth = meta.getAuthor() == null ? "Unknown" : ChatColor.stripColor(meta.getAuthor());
                List<String> pages = meta.getPages();
                boolean noPages = (pages.isEmpty()) || ((pages.size() == 1) && (pages.get(0).isEmpty()));
                
                if (noPages) {
                    p.sendMessage(ChatColor.GRAY + "Sadly, there are no pages for you to read.");
                } else {
                    OpenBook openbook = new OpenBook(title, auth, pages, p.getLocation());
                    openbook.getPagePrintout(p, 0);
                    
                    OpenBook oldBook = this.readers.put(p.getName(), openbook);
                    if (oldBook != null) oldBook.cancelTask();

                    openbook.setTask(Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> BookSaveListener.this.readers.remove(p.getName())
                            , 6000L));
                }

            }
        } else {
            if (a == InventoryAction.NOTHING) return;
            if (a == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                return;
            }
            int slot;
            if (event.getRawSlot() <= 8) {
                slot = event.getRawSlot();
            } else {
                if (a == InventoryAction.MOVE_TO_OTHER_INVENTORY) { slot = inv.firstEmpty(); } else { return; }
            }
            if ((a == InventoryAction.MOVE_TO_OTHER_INVENTORY) && (inv.getItem(slot) == null)) {
                type = event.getCurrentItem().getType();
            } else if (a == InventoryAction.HOTBAR_SWAP) {
                ItemStack hotbar = p.getInventory().getItem(event.getHotbarButton());
                type = hotbar == null ?
                       event.getCurrentItem().getType() :
                       p.getInventory().getItem(event.getHotbarButton()).getType();
            } else if (event.getCursor().getType() != Material.AIR) {
                type = event.getCursor().getType();
            }
            if ((type != null) &&
                    (type != Material.BOOK) && (type != Material.PAPER) && (type != Material.WRITTEN_BOOK) &&
                    (type != Material.BOOK_AND_QUILL) && (type != Material.EMPTY_MAP)) {
                event.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();

        if ((inv.getHolder() instanceof BookShelf)) {
            BookShelf shelf = (BookShelf) inv.getHolder();
            Player p = (Player) event.getPlayer();
            shelf.removeViewer(p);

            System.out.println(inv.getViewers());
            if (inv.getViewers().size() <= 1) {
                if (!Arrays.equals(changetracker.get(shelf.getLocation()), shelf.getInventory().getContents())) {
                    shelf.setChanged();
                }
                changetracker.remove(shelf.getLocation());
                shelf.close();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        Block b = event.getBlock();

        if (b.getType() != Material.BOOKSHELF) return;
        if (BookShelf.hasBookShelf(b)) {
            BookShelf shelf = BookShelf.getBookshelf(b);
            shelf.remove(!this.plugin.shelvesBurnClean);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block b = event.getBlock();

        if (b.getType() != Material.BOOKSHELF) return;
        if (BookShelf.hasBookShelf(b)) {
            BookShelf shelf = BookShelf.getBookshelf(b);
            shelf.remove(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().stream().filter(b -> (BookShelf.hasBookShelf(b))).forEach(b -> {
            BookShelf shelf = BookShelf.getBookshelf(b);
            shelf.remove(true);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBookcasePush(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        blocks.stream().filter(b -> b.getType() == Material.BOOKSHELF).forEach(b -> event.setCancelled(true));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBookcasePull(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return;

        Block b = event.getBlock().getRelative(event.getDirection(), 2);
        if (b.getType() == Material.BOOKSHELF) event.setCancelled(true);
    }
}