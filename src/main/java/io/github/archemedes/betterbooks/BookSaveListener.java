package io.github.archemedes.betterbooks;

import com.google.common.collect.Maps;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookSaveListener implements Listener {
    private final BetterBooks plugin;
    HashMap<String, OpenBook> readers = new HashMap<>();



    public BookSaveListener(BetterBooks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getPlayer().isSneaking()) {
            return;
        }
        Block b = event.getClickedBlock();
        Player p = event.getPlayer();

        if ((p.getEquipment() != null && p.getEquipment().getItemInMainHand().getType() == Material.BOOKSHELF) || (p.isSneaking() && event.isBlockInHand())) {
            return;
        }

        Action a = event.getAction();
        if (a == Action.RIGHT_CLICK_BLOCK && b != null && b.getType() == Material.BOOKSHELF &&
                (event.getBlockFace() != BlockFace.UP) && (event.getBlockFace() != BlockFace.DOWN) ) {
        	
        	if(this.readers.containsKey(p.getName())) {
        		p.sendMessage(ChatColor.GRAY+ "You put down the book you were reading to pick up another.");
        		this.removeReader(p.getName(), false);
        	}

        	boolean playerCanView = true;
        	boolean lockedFromPlayer = false;
            if (Bukkit.getPluginManager().isPluginEnabled("LWC")) {
                LWCPlugin lwcPlugin = (LWCPlugin) plugin.getServer().getPluginManager().getPlugin("LWC");
                if (lwcPlugin != null) { // Sanity check
                    Protection prot = lwcPlugin.getLWC().findProtection(b.getLocation());
                    if (prot != null) {
                        Protection.Type type = prot.getType();

                        if (!(prot.isOwner(p) || prot.isRealOwner(p) || type.equals(Protection.Type.PUBLIC))) {
                            lockedFromPlayer = true;
                        } else if (type.equals(Protection.Type.PASSWORD)) {
                            lockedFromPlayer = true;
                            for (Permission perm : prot.getPermissions()) {
                                if (perm.getAccess().equals(Permission.Access.PLAYER) && perm.getAccess().name().equals(p.getName())) {
                                    lockedFromPlayer = false;
                                    break;
                                }
                            }
                        }

                        if (!(!lockedFromPlayer                     ||
                              type.equals(Protection.Type.DONATION) ||
                              type.equals(Protection.Type.DISPLAY)  )) {
                            playerCanView = false;
                        }
                    }
                }
            }

            event.setCancelled(true);
            if (p.getGameMode() == GameMode.CREATIVE && !p.hasPermission("betterbooks.creative")) {
                p.sendMessage(ChatColor.RED + "You may not open bookshelves in creative mode.");
            } else if (playerCanView) {
                if (lockedFromPlayer) {
                    if (BookShelf.hasBookShelf(b)) {
                        p.sendMessage(ChatColor.RED + "You may only read through the contents of this bookshelf.");
                        BookShelf shelf = BookShelf.getBookshelf(b);
                        if (shelf != null) {
                            shelf.addViewer(p);
                            p.openInventory(shelf.getInventory());
                        } else {
                            throw new NullPointerException("Tried to open a shelf on a block where it doesn't exist.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You may not access this bookshelf at present.");
                    }
                } else {
                    BookShelf shelf = BookShelf.getBookshelf(b);
                    if (shelf == null) {
                        return;
                    }
                    Inventory inv = shelf.getInventory();
                    if (shelf.getInventory().getViewers().size() == 0) {
                        changetracker.put(shelf.getLocation(), inv.getContents());
                    }
                    p.openInventory(inv);
                }
            }
        }
    }

    private HashMap<Location, ItemStack[]> changetracker = Maps.newHashMap();

    @EventHandler(ignoreCancelled = true)
    public void onInteract(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        
        if (!(inv.getHolder() instanceof BookShelf)) {
            return;
        }
        
        BookShelf shelf = (BookShelf) inv.getHolder();
        final Player p = (Player) event.getWhoClicked();
        InventoryAction a = event.getAction();

        if (shelf.isViewer(p)) {
            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, p::closeInventory);
            if ((a == InventoryAction.PICKUP_ALL || a == InventoryAction.PICKUP_HALF || a == InventoryAction.PICKUP_SOME || a == InventoryAction.PICKUP_ONE) 
            		&& ((event.getCurrentItem().getType() == Material.WRITTEN_BOOK) || (event.getCurrentItem().getType() == Material.WRITABLE_BOOK))) {
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

                    openbook.setTask(Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> BookSaveListener.this.removeReader(p.getName(), true)
                            , 60*20*BetterBooks.READER_TIMEOUT_MINUTES));
                }
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
            
            if (inv.getViewers().size() <= 1) {
            	//Last person to close the bookcase cleans it up
            	//This involves collecting everything that isn't a book.
            	ItemStack[] ises = shelf.cleanupForbiddenItems();
            	if(ises.length > 0) {
            		p.sendMessage(ChatColor.LIGHT_PURPLE + "You take out all the items not belonging in a bookcase");
            		Map<Integer, ItemStack> remainder = p.getInventory().addItem(ises);
            		remainder.values().stream()
            		.forEach(is -> p.getWorld().dropItemNaturally(p.getLocation(), is));
            	}
            	
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

	public void removeReader(String name, boolean timeout) {
		if (timeout)
			this.readers.remove(name);
		else
			this.readers.remove(name).cancelTask();
		Player p = plugin.getServer().getPlayer(name);
		if (p != null) p.sendMessage(ChatColor.GRAY + "You put down the book you were reading.");
		
	}
}