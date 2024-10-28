package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Spongee extends JavaPlugin implements Listener {

    private int radius;  // Radius within which the sponge absorbs water
    private Logger logger;  // Custom logger for the plugin
    private Set<String> absorbedBlocks;  // To track absorbed water blocks

    @Override
    public void onEnable() {
        // Initialize the logger with a custom name
        this.logger = Logger.getLogger("log");

        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // Load the config file or create it with default values if it doesn't exist
        createConfig();

        // Get the radius from the configuration (default to 5 if not specified)
        radius = getConfiguration().getInt("absorb-radius", 5);

        // Initialize the set to track absorbed blocks
        absorbedBlocks = new HashSet<>();

        this.logger.info("SpongeWaterAbsorbPlugin enabled! Absorb radius: " + radius);
    }

    @Override
    public void onDisable() {
        this.logger.info("SpongeWaterAbsorbPlugin disabled!");
    }

    @EventHandler
    public void onSpongePlace(BlockPlaceEvent event) {
        // Only proceed if the placed block is a Sponge
        Block block = event.getBlock();
        if (block.getTypeId() == 19) {  // Sponge block ID
            // Replace water blocks within a radius around the sponge
            absorbWater(block);
        }
    }

    private void absorbWater(Block spongeBlock) {
        // Get the location of the sponge block
        int spongeX = spongeBlock.getX();
        int spongeY = spongeBlock.getY();
        int spongeZ = spongeBlock.getZ();
        
        // Iterate through a cube area around the sponge to find water blocks
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                // Limiting the height to Y+1, Y-1, to avoid unnecessary checks
                for (int z = -radius; z <= radius; z++) {
                    // Calculate the location of the block to check
                    Block currentBlock = spongeBlock.getWorld().getBlockAt(spongeX + x, spongeY + y, spongeZ + z);
                    
                    // Check if the current block is still water (ID 8) or flowing water (ID 9)
                    if ((currentBlock.getTypeId() == 8 || currentBlock.getTypeId() == 9)) {
                        String blockKey = currentBlock.getLocation().toString();

                        // Only absorb water if this block has not been absorbed before
                        if (!absorbedBlocks.contains(blockKey)) {
                            // Replace the water with air (ID 0 is air)
                            currentBlock.setTypeId(0);  // Set to air
                            
                            // Mark this block as absorbed
                            absorbedBlocks.add(blockKey);
                        }
                    }
                }
            }
        }
    }

    // Method to create the config file with default values if it doesn't exist
    private void createConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        // Check if the config file exists
        if (!configFile.exists()) {
            getDataFolder().mkdirs();  // Ensure the folder exists

            // Write default content to config.yml
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("# Configuration for Sponge Water Absorb plugin\n");
                writer.write("# The radius in which the sponge will absorb water\n");
                writer.write("absorb-radius: 5\n");

                this.logger.info("config.yml created with default values.");
            } catch (IOException e) {
                this.logger.severe("Could not create config.yml in the plugin folder.");
                e.printStackTrace();
            }
        } else {
            this.logger.info("config.yml already exists.");
        }
    }
}
