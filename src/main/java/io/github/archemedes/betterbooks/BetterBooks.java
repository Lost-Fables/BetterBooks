package io.github.archemedes.betterbooks;

import io.github.archemedes.betterbooks.sqlite.Database;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterBooks extends JavaPlugin {
    boolean shelvesBurnClean;
    boolean signOnCompletion;

    private boolean omniscienceEnabled = false;
    
    BookSaveListener bsl;

    public static Database db;
    public static final int READER_TIMEOUT_MINUTES = 5;
    public static final boolean DEBUGGING = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
    	bsl = new BookSaveListener(this);
    	
        getCommand("signbook").setExecutor(new BookSigner(this));
        getCommand("browsebook").setExecutor(new BookBrowser(bsl.readers, this));
        PluginManager pm = Bukkit.getPluginManager();

        db = new Database(this);
        db.load();
        
        pm.registerEvents(bsl, this);
        pm.registerEvents(new BookCraftListener(this), this);

        FileConfiguration config = getConfig();

        this.shelvesBurnClean = config.getBoolean("shelves.burn.clean");
        this.signOnCompletion = config.getBoolean("sign.on.completion");

        pm.registerEvents(new BlockLoggingListener(), this);
    }

    @Override
    public void onDisable() {
        for (BookShelf shelf : BookShelf.getAllShelves().values()) {
            shelf.close();
        }
    }
}