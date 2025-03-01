package ca.jessewhite.imaginationplugin;

import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ImaginationPlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    PaperLib.suggestPaper(this);
    saveDefaultConfig();
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onPlayerSpawnChicken(PlayerInteractEvent event) {
    // Only respond to right-clicks with a chicken egg
    if (EquipmentSlot.HAND.equals(event.getHand())) {
      ItemStack item = event.getItem();
      if (item != null && Material.CHICKEN_SPAWN_EGG.equals(item.getType())) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 1, 0);

        // Spawn the chicken
        Chicken chicken = world.spawn(loc, Chicken.class);
        player.sendMessage("You have spawned an exploding chicken!");

        // Schedule explosion after 3 seconds
        new BukkitRunnable() {
          @Override
          public void run() {
            if (chicken.isValid()) {
              world.createExplosion(chicken.getLocation(), 4.0F, false, false);
              chicken.remove();
            }
          }
        }.runTaskLater(this, 60L); // 60 ticks = 3 seconds
      }
    }
  }
}
