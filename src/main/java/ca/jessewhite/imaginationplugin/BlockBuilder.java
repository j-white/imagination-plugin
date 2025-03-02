package ca.jessewhite.imaginationplugin;

import ca.jessewhite.imaginationplugin.ai.Block;
import ca.jessewhite.imaginationplugin.ai.Blocks;
import ca.jessewhite.imaginationplugin.ai.MinecraftBlocks;
import ca.jessewhite.imaginationplugin.ai.MinecraftElement;
import ca.jessewhite.imaginationplugin.ai.Position;
import ca.jessewhite.imaginationplugin.ai.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BlockBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(BlockBuilder.class);
    private final JavaPlugin plugin;
    private static final Map<String, Material> materialCache = new HashMap<>();
    // Distance in front of player to start building - increased to prevent player from being in structure
    private static final int BUILD_DISTANCE = 5;
    // Height offset to ensure building above ground
    private static final int Y_OFFSET = 1;
    // Safety distance to ensure player is not inside the structure
    private static final int SAFETY_DISTANCE = 2;

    public BlockBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void build(MinecraftBlocks blocks) {
        // Schedule the building task to run on the main server thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (blocks == null || blocks.elements() == null || blocks.elements().isEmpty()) {
                LOG.warn("No elements to build");
                return;
            }

            // Get the player who initiated the command
            Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            if (player == null) {
                LOG.warn("No player found to build near");
                return;
            }

            // Calculate a position in front of the player based on where they're looking
            Location playerLoc = player.getLocation();
            World world = playerLoc.getWorld();
            Vector direction = playerLoc.getDirection().normalize();
            
            // Get a position several blocks in front of the player to ensure safety
            Location buildStartLoc = playerLoc.clone().add(direction.clone().multiply(BUILD_DISTANCE));
            
            // Round to block coordinates (integers)
            buildStartLoc.setX(Math.floor(buildStartLoc.getX()));
            buildStartLoc.setY(Math.floor(buildStartLoc.getY()));
            buildStartLoc.setZ(Math.floor(buildStartLoc.getZ()));
            
            // Find the highest block at this location to build on top of it
            int groundY = world.getHighestBlockYAt(buildStartLoc.getBlockX(), buildStartLoc.getBlockZ());
            buildStartLoc.setY(groundY + Y_OFFSET);

            // Get bounds of the structure to ensure player safety
            BoundingBox structureBounds = calculateStructureBounds(blocks, buildStartLoc);
            
            // Check if player is too close to the building area
            if (isPlayerTooClose(playerLoc, structureBounds)) {
                // Adjust the building location to ensure player safety
                Vector safetyOffset = direction.clone().multiply(SAFETY_DISTANCE);
                buildStartLoc.add(safetyOffset);
                LOG.info("Adjusted building position to ensure player safety");
            }

            LOG.info("Building structure with " + blocks.elements().size() + " elements in front of player at " +
                    buildStartLoc.getBlockX() + ", " + buildStartLoc.getBlockY() + ", " + buildStartLoc.getBlockZ() +
                    " (ground level: " + groundY + ")");

            // Process each element in the structure
            for (MinecraftElement element : blocks.elements()) {
                try {
                    if ("block".equalsIgnoreCase(element.elementType())) {
                        // Single block placement
                        Position pos = element.position();
                        if (pos != null) {
                            placeBlock(
                                world,
                                buildStartLoc.getBlockX() + (int)Math.round(pos.x()),
                                buildStartLoc.getBlockY() + (int)Math.round(pos.y()),
                                buildStartLoc.getBlockZ() + (int)Math.round(pos.z()),
                                element.material()
                            );
                        } else {
                            LOG.warn("Block element missing position");
                        }
                    } else if ("area".equalsIgnoreCase(element.elementType())) {
                        // Area fill
                        Range range = element.range();
                        if (range != null) {
                            fillArea(
                                world,
                                buildStartLoc.getBlockX() + (int)Math.round(range.x().min()),
                                buildStartLoc.getBlockY() + (int)Math.round(range.y().min()),
                                buildStartLoc.getBlockZ() + (int)Math.round(range.z().min()),
                                buildStartLoc.getBlockX() + (int)Math.round(range.x().max()),
                                buildStartLoc.getBlockY() + (int)Math.round(range.y().max()),
                                buildStartLoc.getBlockZ() + (int)Math.round(range.z().max()),
                                element.material()
                            );
                        } else {
                            LOG.warn("Area element missing range");
                        }
                    } else {
                        LOG.warn("Unknown element type: " + element.elementType());
                    }
                } catch (Exception e) {
                    LOG.warn("Error placing element: " + e.getMessage(), e);
                }
            }
            LOG.info("Structure building complete");
        });
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

            // Calculate a position in front of the player based on where they're looking
            Location playerLoc = player.getLocation();
            World world = playerLoc.getWorld();
            Vector direction = playerLoc.getDirection().normalize();
            
            // Get a position several blocks in front of the player to ensure safety
            Location buildStartLoc = playerLoc.clone().add(direction.clone().multiply(BUILD_DISTANCE));
            
            // Round to block coordinates (integers)
            buildStartLoc.setX(Math.floor(buildStartLoc.getX()));
            buildStartLoc.setY(Math.floor(buildStartLoc.getY()));
            buildStartLoc.setZ(Math.floor(buildStartLoc.getZ()));
            
            // Find the highest block at this location to build on top of it
            int groundY = world.getHighestBlockYAt(buildStartLoc.getBlockX(), buildStartLoc.getBlockZ());
            buildStartLoc.setY(groundY + Y_OFFSET);

            // Get bounds of the structure to ensure player safety
            BoundingBox structureBounds = calculateStructureBounds(blocks, buildStartLoc);
            
            // Check if player is too close to the building area
            if (isPlayerTooClose(playerLoc, structureBounds)) {
                // Adjust the building location to ensure player safety
                Vector safetyOffset = direction.clone().multiply(SAFETY_DISTANCE);
                buildStartLoc.add(safetyOffset);
                LOG.info("Adjusted building position to ensure player safety");
            }

            LOG.info("Building structure with " + blocks.blocks().size() + " blocks in front of player at " +
                    buildStartLoc.getBlockX() + ", " + buildStartLoc.getBlockY() + ", " + buildStartLoc.getBlockZ() +
                    " (ground level: " + groundY + ")");

            // Process each block in the structure
            for (Block block : blocks.blocks()) {
                try {
                    if (block.fill()) {
                        // Check if the end coordinates are available
                        if (block.endX() != null && block.endY() != null && block.endZ() != null) {
                            // Create a fill block and process it
                            fillArea(
                                    world,
                                    buildStartLoc.getBlockX() + (int)Math.round(block.x()),
                                    buildStartLoc.getBlockY() + (int)Math.round(block.y()),
                                    buildStartLoc.getBlockZ() + (int)Math.round(block.z()),
                                    buildStartLoc.getBlockX() + (int)Math.round(block.endX()),
                                    buildStartLoc.getBlockY() + (int)Math.round(block.endY()),
                                    buildStartLoc.getBlockZ() + (int)Math.round(block.endZ()),
                                    block.type()
                            );
                        } else {
                            LOG.warn("Fill block missing end coordinates at " + block.x() + "," + block.y() + "," + block.z());
                            // Still place a single block
                            placeBlock(
                                    world,
                                    buildStartLoc.getBlockX() + (int)Math.round(block.x()),
                                    buildStartLoc.getBlockY() + (int)Math.round(block.y()),
                                    buildStartLoc.getBlockZ() + (int)Math.round(block.z()),
                                    block.type()
                            );
                        }
                    } else {
                        // Regular block placement
                        placeBlock(
                                world,
                                buildStartLoc.getBlockX() + (int)Math.round(block.x()),
                                buildStartLoc.getBlockY() + (int)Math.round(block.y()),
                                buildStartLoc.getBlockZ() + (int)Math.round(block.z()),
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

    // Added helper class to represent a bounding box for the structure
    private static class BoundingBox {
        int minX, minY, minZ;
        int maxX, maxY, maxZ;

        BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        boolean contains(int x, int y, int z) {
            return x >= minX && x <= maxX &&
                   y >= minY && y <= maxY &&
                   z >= minZ && z <= maxZ;
        }
    }

    // Calculate the bounding box of the structure
    private BoundingBox calculateStructureBounds(MinecraftBlocks blocks, Location buildStartLoc) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (MinecraftElement element : blocks.elements()) {
            if ("block".equalsIgnoreCase(element.elementType())) {
                Position pos = element.position();
                if (pos != null) {
                    int x = buildStartLoc.getBlockX() + (int)Math.round(pos.x());
                    int y = buildStartLoc.getBlockY() + (int)Math.round(pos.y());
                    int z = buildStartLoc.getBlockZ() + (int)Math.round(pos.z());
                    
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                }
            } else if ("area".equalsIgnoreCase(element.elementType())) {
                Range range = element.range();
                if (range != null) {
                    int startX = buildStartLoc.getBlockX() + (int)Math.round(range.x().min());
                    int startY = buildStartLoc.getBlockY() + (int)Math.round(range.y().min());
                    int startZ = buildStartLoc.getBlockZ() + (int)Math.round(range.z().min());
                    int endX = buildStartLoc.getBlockX() + (int)Math.round(range.x().max());
                    int endY = buildStartLoc.getBlockY() + (int)Math.round(range.y().max());
                    int endZ = buildStartLoc.getBlockZ() + (int)Math.round(range.z().max());
                    
                    minX = Math.min(minX, Math.min(startX, endX));
                    minY = Math.min(minY, Math.min(startY, endY));
                    minZ = Math.min(minZ, Math.min(startZ, endZ));
                    maxX = Math.max(maxX, Math.max(startX, endX));
                    maxY = Math.max(maxY, Math.max(startY, endY));
                    maxZ = Math.max(maxZ, Math.max(startZ, endZ));
                }
            }
        }
        
        // If no elements were processed, provide a default small bounding box
        if (minX == Integer.MAX_VALUE) {
            return new BoundingBox(
                buildStartLoc.getBlockX(), buildStartLoc.getBlockY(), buildStartLoc.getBlockZ(),
                buildStartLoc.getBlockX(), buildStartLoc.getBlockY(), buildStartLoc.getBlockZ()
            );
        }
        
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // Calculate the bounding box of the structure for Blocks
    private BoundingBox calculateStructureBounds(Blocks blocks, Location buildStartLoc) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Block block : blocks.blocks()) {
            int x = buildStartLoc.getBlockX() + (int)Math.round(block.x());
            int y = buildStartLoc.getBlockY() + (int)Math.round(block.y());
            int z = buildStartLoc.getBlockZ() + (int)Math.round(block.z());
            
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
            
            if (block.fill() && block.endX() != null && block.endY() != null && block.endZ() != null) {
                int endX = buildStartLoc.getBlockX() + (int)Math.round(block.endX());
                int endY = buildStartLoc.getBlockY() + (int)Math.round(block.endY());
                int endZ = buildStartLoc.getBlockZ() + (int)Math.round(block.endZ());
                
                minX = Math.min(minX, endX);
                minY = Math.min(minY, endY);
                minZ = Math.min(minZ, endZ);
                maxX = Math.max(maxX, endX);
                maxY = Math.max(maxY, endY);
                maxZ = Math.max(maxZ, endZ);
            }
        }
        
        // If no blocks were processed, provide a default small bounding box
        if (minX == Integer.MAX_VALUE) {
            return new BoundingBox(
                buildStartLoc.getBlockX(), buildStartLoc.getBlockY(), buildStartLoc.getBlockZ(),
                buildStartLoc.getBlockX(), buildStartLoc.getBlockY(), buildStartLoc.getBlockZ()
            );
        }
        
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // Check if player is too close to the building area
    private boolean isPlayerTooClose(Location playerLoc, BoundingBox bounds) {
        // Expand the bounds slightly to provide a safety buffer
        BoundingBox expandedBounds = new BoundingBox(
            bounds.minX - SAFETY_DISTANCE,
            bounds.minY - SAFETY_DISTANCE,
            bounds.minZ - SAFETY_DISTANCE,
            bounds.maxX + SAFETY_DISTANCE,
            bounds.maxY + SAFETY_DISTANCE,
            bounds.maxZ + SAFETY_DISTANCE
        );
        
        return expandedBounds.contains(playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ());
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
