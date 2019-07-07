package io.github.archemedes.betterbooks;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
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
                ItemUtil.setCustomTag(meta, "archebook", p.getUniqueId().toString());
                e.setNewBookMeta(meta);
            }
        }
    }
}