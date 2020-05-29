package io.github.archemedes.betterbooks;

import co.lotc.core.bukkit.util.ItemUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class BookCraftListener implements Listener {
    private final BetterBooks plugin;
    private final PersonaHandler handler;

    public BookCraftListener(BetterBooks plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().isPluginEnabled("RPPersonas")) {
            handler = RPPersonas.get().getPersonaHandler();
        } else {
            handler = null;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSign(PlayerEditBookEvent e) {
        if (e.isSigning()) {
            Player p = e.getPlayer();
            if (this.plugin.signOnCompletion && this.handler != null) {
                Persona pers = this.handler.getLoadedPersona(p);
                if (pers != null) {
                    BookMeta meta = e.getNewBookMeta();
                    meta.setAuthor(ChatColor.AQUA + pers.getNickName());
                    ItemUtil.setCustomTag(meta, "archebook", p.getUniqueId().toString());
                    e.setNewBookMeta(meta);
                }
            }
        }
    }
}