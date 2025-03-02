package ca.jessewhite.imaginationplugin;

import ca.jessewhite.imaginationplugin.ai.Block;
import ca.jessewhite.imaginationplugin.ai.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BlockBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(BlockBuilder.class);
    private final JavaPlugin plugin;
    private static final Map<String, Material> materialCache = new HashMap<>();

    public BlockBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void build(Blocks blocks) {
        // Schedule the building task to run on the main server thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (blocks == null || blocks.blocks() == null || blocks.blocks().isEmpty()) {
                LOG.warn("No blocks to build");
                return;
            }

            // Get the player who initiated the command
            Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            if (player == null) {
                LOG.warn("No player found to build near");
                return;
            }

            // Use the player's location as the starting point
            Location startLoc = player.getLocation();
            World world = startLoc.getWorld();

            LOG.info("Building structure with " + blocks.blocks().size() + " blocks near " +
                    startLoc.getBlockX() + ", " + startLoc.getBlockY() + ", " + startLoc.getBlockZ());

            // Process each block in the structure
            for (Block block : blocks.blocks()) {
                try {
                    if (block.fill()) {
                        // Check if the end coordinates are available
                        if (block.endX() != null && block.endY() != null && block.endZ() != null) {
                            // Create a fill block and process it
                            fillArea(
                                    world,
                                    startLoc.getBlockX() + block.x(),
                                    startLoc.getBlockY() + block.y(),
                                    startLoc.getBlockZ() + block.z(),
                                    startLoc.getBlockX() + block.endX(),
                                    startLoc.getBlockY() + block.endY(),
                                    startLoc.getBlockZ() + block.endZ(),
                                    block.type()
                            );
                        } else {
                            LOG.warn("Fill block missing end coordinates at " + block.x() + "," + block.y() + "," + block.z());
                            // Still place a single block
                            placeBlock(
                                    world,
                                    startLoc.getBlockX() + block.x(),
                                    startLoc.getBlockY() + block.y(),
                                    startLoc.getBlockZ() + block.z(),
                                    block.type()
                            );
                        }
                    } else {
                        // Regular block placement
                        placeBlock(
                                world,
                                startLoc.getBlockX() + block.x(),
                                startLoc.getBlockY() + block.y(),
                                startLoc.getBlockZ() + block.z(),
                                block.type()
                        );
                    }
                } catch (Exception e) {
                    LOG.warn("Error placing block: " + e.getMessage(), e);
                }
            }
            LOG.info("Structure building complete");
        });
    }

    private void fillArea(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, String blockType) {
        // Ensure the coordinates are in order (min to max)
        int minX = Math.min(startX, endX);
        int minY = Math.min(startY, endY);
        int minZ = Math.min(startZ, endZ);
        int maxX = Math.max(startX, endX);
        int maxY = Math.max(startY, endY);
        int maxZ = Math.max(startZ, endZ);

        LOG.info("Filling area from (" + minX + "," + minY + "," + minZ + ") to (" +
                maxX + "," + maxY + "," + maxZ + ") with " + blockType);

        // Loop through each position in the 3D region
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    placeBlock(world, x, y, z, blockType);
                }
            }
        }
    }

    private void placeBlock(World world, int x, int y, int z, String blockType) {
        Material material = getMaterial(blockType);
        if (material == null || material == Material.AIR) {
            // For air, we just set the block directly
            world.getBlockAt(x, y, z).setType(Material.AIR);
            return;
        }

        try {
            // Place the block
            world.getBlockAt(x, y, z).setType(material);
        } catch (Exception e) {
            LOG.warn("Failed to place block " + material + " at " + x + "," + y + "," + z, e);
        }
    }

    private Material getMaterial(String blockType) {
        // Remove minecraft: prefix if present
        String cleanType = blockType.replace("minecraft:", "").toUpperCase();

        // Check cache first
        if (materialCache.containsKey(cleanType)) {
            return materialCache.get(cleanType);
        }

        try {
            // Try to get the material directly
            Material material = Material.valueOf(cleanType);
            materialCache.put(cleanType, material);
            return material;
        } catch (IllegalArgumentException e) {
            // Log the error and try alternative approaches
            LOG.warn("Could not find material for: " + cleanType);

            // For unknown blocks, default to a visible placeholder (can be customized)
            materialCache.put(cleanType, Material.STONE);
            return Material.STONE;
        }
    }
}
