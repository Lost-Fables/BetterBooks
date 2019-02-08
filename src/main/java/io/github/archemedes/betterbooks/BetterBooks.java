package io.github.archemedes.betterbooks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterBooks extends JavaPlugin {
    boolean shelvesBurnClean;
    boolean signOnCompletion;

    private boolean hawkeyeEnabled = false;
    
    BookSaveListener bsl;
    
    public static final int READER_TIMEOUT_MINUTES = 5;

    @Override
    public void onEnable() {
    	bsl = new BookSaveListener(this);
    	
        getCommand("signbook").setExecutor(new BookSigner(this));
        getCommand("fixbook").setExecutor(new BookFixer(this));
        getCommand("browsebook").setExecutor(new BookBrowser(bsl.readers, this));
        PluginManager pm = Bukkit.getPluginManager();
        
        
        
        pm.registerEvents(bsl, this);
        pm.registerEvents(new BookCraftListener(this), this);
        BookShelf.init(this);

        FileConfiguration config = getConfig();
        saveDefaultConfig();

        this.shelvesBurnClean = config.getBoolean("shelves.burn.clean");
        this.signOnCompletion = config.getBoolean("sign.on.completion");

        if (getServer().getPluginManager().getPlugin("HawkEye") != null) {
            try {
                Class.forName("uk.co.oliwali.HawkEye.entry.containerentries.ContainerExtract");
                hawkeyeEnabled = true;
                pm.registerEvents(new BlockLoggingListener(), this);
            } catch (ClassNotFoundException ignored) {}
        }

    }

    @Override
    public void onDisable() {
        for (BookShelf shelf : BookShelf.getAllShelves().values()) {
            shelf.close();
        }
    }

    public boolean isHawkeyeEnabled() {
        return hawkeyeEnabled;
    }
}