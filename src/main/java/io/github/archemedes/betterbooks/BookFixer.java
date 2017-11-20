package io.github.archemedes.betterbooks;

import io.github.archemedes.customitem.CustomTag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BookFixer implements CommandExecutor {

    public BookFixer(BetterBooks betterBooks) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;
        int count = 0;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is != null)
                if (is.getType() == Material.WRITTEN_BOOK) {
                    if (is.getDurability() != 0) {
                        is.setDurability((short) 0);
                        count += is.getAmount();
                        CustomTag t = CustomTag.getFrom(is);
                        t.put("archebook", p.getUniqueId().toString());
                        is = t.apply(is);
                        p.getInventory().setItem(i, is);
                    }
                }
        }
        if (count == 0) {
            p.sendMessage(ChatColor.RED + "No broken books were found in your inventory.");
        } else {
            p.sendMessage(ChatColor.GREEN + "Fixed " + count + " broken books.");
        }
        return true;

    }

}
