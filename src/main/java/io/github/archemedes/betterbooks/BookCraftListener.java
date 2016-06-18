package io.github.archemedes.betterbooks;

import io.github.archemedes.customitem.Customizer;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookCraftListener
        implements Listener {
    private final BetterBooks plugin;
    private final PersonaHandler handler;

    public BookCraftListener(BetterBooks plugin) {
        this.plugin = plugin;
        this.handler = ArcheCore.getControls().getPersonaHandler();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSign(PlayerEditBookEvent e) {
        if (e.isSigning()) {
            Player p = e.getPlayer();
            if ((this.plugin.signOnCompletion) && (this.handler.hasPersona(p))) {
                BookMeta meta = e.getNewBookMeta();
                meta.setAuthor(ChatColor.AQUA + this.handler.getPersona(p).getName());
                e.setNewBookMeta(meta);
                ItemStack is = p.getInventory().getItem(e.getSlot());
                is = Customizer.giveCustomTag(is, "archebook");
                e.getPlayer().getInventory().setItem(e.getSlot(), is);
            }
        }
    }
}