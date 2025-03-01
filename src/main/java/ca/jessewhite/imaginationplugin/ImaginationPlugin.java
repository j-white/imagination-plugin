package ca.jessewhite.imaginationplugin;

import io.papermc.lib.PaperLib;
import org.bukkit.plugin.java.JavaPlugin;

public class ImaginationPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    PaperLib.suggestPaper(this);

    saveDefaultConfig();
  }
}
