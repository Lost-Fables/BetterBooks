package io.github.archemedes.betterbooks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterBooks extends JavaPlugin {
    boolean shelvesBurnClean;
    boolean signOnCompletion;

    private boolean hawkeyeEnabled = false;

    @Override
    public void onEnable() {
    	BookSaveListener bsl = new BookSaveListener(this);
    	
        getCommand("signbook").setExecutor(new BookSigner(this));
        getCommand("fixbook").setExecutor(new BookFixer(this));
        getCommand("browsebook").setExecutor(new BookBrowser(this, bsl.readers));
        PluginManager pm = Bukkit.getPluginManager();
        
        
        
        pm.registerEvents(bsl, this);
        pm.registerEvents(new BookCraftListener(this), this);
        BookShelf.init(this);

        FileConfiguration config = getConfig();
        saveDefaultConfig();

        this.shelvesBurnClean = config.getBoolean("shelves.burn.clean");
        this.signOnCompletion = config.getBoolean("sign.on.completion");

        if (getServer().getPluginManager().getPlugin("HawkEye") != null) {
            hawkeyeEnabled = true;
            pm.registerEvents(new HawkEyeListener(), this);

        }

    }

    @Override
    public void onDisable() {
        for (BookShelf shelf : BookShelf.getAllShelves().values()) {
            shelf.close();
        }

        ClearShelfTask.close();
    }

    public boolean isHawkeyeEnabled() {
        return hawkeyeEnabled;
    }
}