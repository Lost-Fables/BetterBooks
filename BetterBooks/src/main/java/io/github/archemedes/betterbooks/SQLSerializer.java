package io.github.archemedes.betterbooks;

import io.github.archemedes.customitem.Customizer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SQLSerializer {
    public static PreparedStatement serialize(PreparedStatement into, ItemStack item)
            throws SQLException {
        Material m = item.getType();
        int amt = item.getAmount();
        short dmg = item.getDurability();
        String tit = null;
        String lore = null;
        String auth = null;
        String page = null;

        List<String> lores = item.getItemMeta().getLore();
        if (lores != null) {
            StringBuilder loreBuffer = new StringBuilder(128);
            for (String s : lores) {
                loreBuffer.append(s.replace("$", "")).append("$");
            }
            loreBuffer.delete(loreBuffer.length() - 1, loreBuffer.length());
            lore = loreBuffer.toString();
        }

        if ((m == Material.WRITTEN_BOOK) || (m == Material.BOOK_AND_QUILL)) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            tit = meta.getTitle();
            auth = meta.getAuthor();
            List<String> pages = meta.getPages();
            StringBuilder buffer = new StringBuilder();
            for (String s : pages) {
                buffer.append(s.replace("$", "")).append("$");
            }
            if (buffer.length() > 0) buffer.delete(buffer.length() - 1, buffer.length());
            page = buffer.toString();
        } else {
            tit = item.getItemMeta().getDisplayName();
        }

        into.setString(6, m.toString());
        into.setInt(7, amt);
        into.setShort(8, dmg);

        into.setString(9, tit);
        into.setString(10, lore);
        into.setString(11, auth);
        into.setString(12, page);
        return into;
    }

    public static ItemStack deserialize(ResultSet res)
            throws SQLException {
        String mStr = res.getString(6);
        Material m;
        try {
            m = Material.matchMaterial(mStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int amt = res.getInt(7);
        short dmg = res.getShort(8);

        ItemStack book = new ItemStack(m, amt, dmg);

        if ((m != Material.WRITTEN_BOOK) && (m != Material.BOOK) && (m != Material.PAPER) &&
                (m != Material.BOOK_AND_QUILL) && (m != Material.EMPTY_MAP)) {
            System.out.println("[BetterBooks][WARNING] Forbidden Items in bookshelf. Plugin integrity in danger.");
        }

        String title = res.getString(9);
        String lore = res.getString(10);
        String author = res.getString(11);
        String page = res.getString(12);

        if ((m == Material.WRITTEN_BOOK) || (m == Material.BOOK_AND_QUILL)) {
            BookMeta meta = (BookMeta) book.getItemMeta();
            if (title != null) meta.setTitle(title);
            if (author != null) meta.setAuthor(author);

            if (page != null) {
                meta.setPages(page.split("\\$"));
            }
            if (lore != null) {
                List<String> lorelist = Arrays.asList(lore.split("\\$"));
                meta.setLore(lorelist);
            }

            book.setItemMeta(meta);
        } else {
            ItemMeta meta = book.getItemMeta();
            if (lore != null) {
                List<String> lorelist = Arrays.asList(lore.split("\\$"));
                meta.setLore(lorelist);
            }
            if (title != null) {
                meta.setDisplayName(title);
            }
            book.setItemMeta(meta);
        }
        book = Customizer.giveCustomTag(book, "archebook");
        return book;
    }
}