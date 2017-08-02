package io.github.archemedes.betterbooks;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class BookBrowser implements CommandExecutor {
	private Map<String, OpenBook> readers;
	
	public BookBrowser(Map<String, OpenBook> readers) {
		this.readers = readers;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
		if(argv.length == 0) {
			return false;
		}

		//Note this command goes from 1 to n for pages as its more
		//intuitive for the player reading a book.
		int page = 0;
		try {page = Integer.parseInt(argv[0]);} catch(NumberFormatException e) { return false; }
		
		
		String name = sender.getName();
		OpenBook book = readers.get(name);
		if(book != null) {
			Location l = book.getLocation();	
			Player p = (Player) sender;
			if(p.getWorld() != l.getWorld() || l.distance(p.getLocation()) > 5) {
				sender.sendMessage(ChatColor.RED + "You have put back the book as you moved away from the bookshelf");
				return true;
			}
			
			if(page < 1 || page > book.getPages()) {
				sender.sendMessage(ChatColor.RED + "Invalid Page.");
				return true;
			}
			
			book.getPagePrintout(p, --page);
		} else {
			sender.sendMessage("You are not currently reading any books...");
		}

		return true;
	}

}