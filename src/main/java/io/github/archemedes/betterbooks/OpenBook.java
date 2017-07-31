package io.github.archemedes.betterbooks;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

class OpenBook {
    private final String title;
    private final String author;
    private final List<String> pages;
    private final Location location;
    private int task = 0;

    OpenBook(String title, String author, List<String> pages, Location location) {
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.location = location;
    }

    String getTitle() {
        return this.title;
    }

    String getAuthor() {
        return this.author;
    }

    void getPagePrintout(Player p, int page) {
    	//Note this goes from 0..n-1. Browsebook goes from 1 to n
    	String divider = ChatColor.GREEN + "--------------------------------------------------\n";
    	String tagline = ChatColor.GREEN + "Book: " + ChatColor.WHITE + getTitle() + " " + ChatColor.GREEN + "By: " + ChatColor.WHITE + getAuthor() + "\n";
    	
    	p.sendMessage(divider + tagline + divider);
    	
    	p.sendMessage(ChatColor.stripColor(this.pages.get(page)));
        /*String[] words = this.pages.get(page).split(" ");

        StringBuilder bl = new StringBuilder(64);
        for (String word : words) {
            bl.append(word).append(" ");
            if (bl.length() >= 50) {
                p.sendMessage(ChatColor.stripColor(bl.toString()));
                bl = new StringBuilder(64);
            }
        }
        p.sendMessage(bl.toString());*/
    	
        p.sendMessage(divider);
    	
    	TextComponent tc = new TextComponent();
    	tc.setColor(net.md_5.bungee.api.ChatColor.BLUE);
    	
    	//Page navigator
    	TextComponent sub;
    	
    	if(page > 0) {
    		sub = new TextComponent("Previous");
    		tc.addExtra("[");
    		sub.setItalic(true);
    		sub.setColor(net.md_5.bungee.api.ChatColor.GRAY);
			sub.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/browsebook " + (page) ));
    		
			tc.addExtra(sub);
			tc.addExtra("]");
    	}
    	
    	sub = new TextComponent("   Page " + (page+1) + "/" + getPages() + "   ");
    	sub.setColor(net.md_5.bungee.api.ChatColor.GREEN);
    	tc.addExtra(sub);
    	
    	if(page+1 < getPages()) {
    		sub = new TextComponent("Next");
    		tc.addExtra("[");
	    	sub.setItalic(true);
			sub.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/browsebook " + (page+2) ));
	    	sub.setColor(net.md_5.bungee.api.ChatColor.GRAY);
	    	tc.addExtra(sub);
	    	tc.addExtra("]");
	    	
    	}
    	
    	p.spigot().sendMessage(tc);
    }

    int getPages() {
        return this.pages.size();
    }

    int getTask() {
        return this.task;
    }

    void setTask(int task) {
        if (task > 0) this.task = task;
    }
    
    void cancelTask() {
    	if (this.task > 0) Bukkit.getScheduler().cancelTask(this.task);
    }

    Location getLocation() {
        return this.location.clone();
    }
}