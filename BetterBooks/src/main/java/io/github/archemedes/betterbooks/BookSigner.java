package io.github.archemedes.betterbooks;

import io.github.archemedes.customitem.Customizer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSigner
        implements CommandExecutor {
    private BetterBooks plugin;

    public BookSigner(BetterBooks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        StringBuilder authorName = new StringBuilder(32);

        if (!(sender instanceof Player)) {
            this.plugin.getLogger().info("Must be run by a player!");
            return false;
        }

        Player player = (Player) sender;

        if ((argv.length > 4) || (argv.length < 1)) {
            return false;
        }
        for (String arg : argv)
            authorName.append(" ").append(arg);
        authorName.delete(0, 1);
        if (authorName.length() > 32) {
            player.sendMessage(ChatColor.RED + "Get over yourself! You do not need such a hellishly long name!");
            return false;
        }
        if (authorName.equals("null")) {
            player.sendMessage(ChatColor.RED + "Oh you think you're soooo smart huh?");
            return false;
        }
        String author = authorName.toString();

        ItemStack item = player.getEquipment().getItemInMainHand();
        if (item.getType() == Material.WRITTEN_BOOK) {
            if (Customizer.isCustom(item))
                if (!Customizer.getCustomTag(item).equalsIgnoreCase("archebook")) {
                    BookMeta metadata = (BookMeta) item.getItemMeta();
                    if ((metadata.getAuthor().equals(player.getName())) || (player.hasPermission("betterbooks.signothers"))) {
                        metadata.setAuthor(ChatColor.AQUA + author);
                        item.setItemMeta(metadata);
                        item.setDurability((short) 0);
                        player.sendMessage(ChatColor.AQUA + "Book Signed!");
                        item = Customizer.giveCustomTag(item, "archebook");
                        return true;
                    }
                    player.sendMessage(ChatColor.RED + "A sudden onset of morality compels you not to sign another person's book");
                    return true;
                }
            player.sendMessage(ChatColor.RED + "Book has already been signed!");
            return true;
        }
        player.sendMessage(ChatColor.RED + "You can't sign that!");
        return true;
    }
}