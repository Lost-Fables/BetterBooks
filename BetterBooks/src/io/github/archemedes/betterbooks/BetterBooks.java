package io.github.archemedes.betterbooks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterBooks extends JavaPlugin
{
  boolean shelvesBurnClean;
  boolean signOnCompletion;

  @Override
public void onEnable()
  {
    getCommand("signbook").setExecutor(new BookSigner(this));
    getCommand("fixbook").setExecutor(new BookFixer(this));
    PluginManager pm = Bukkit.getPluginManager();
    pm.registerEvents(new BookSaveListener(this), this);
    pm.registerEvents(new BookCraftListener(this), this);
    BookShelf.init(this);

    FileConfiguration config = getConfig();
    saveDefaultConfig();

    this.shelvesBurnClean = config.getBoolean("shelves.burn.clean");
    this.signOnCompletion = config.getBoolean("sign.on.completion");
  }

  @Override
public void onDisable()
  {
    for (BookShelf shelf : BookShelf.getAllShelves().values()) {
      shelf.close();
    }

    ClearShelfTask.close();
  }
}