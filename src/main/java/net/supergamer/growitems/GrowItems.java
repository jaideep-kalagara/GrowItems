package net.supergamer.growitems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.supergamer.growitems.block.ModBlockEntities;
import net.supergamer.growitems.block.ModBlocks;
import net.supergamer.growitems.block.custom.ItemGrowerBlockGrowthTimings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrowItems implements ModInitializer {
    public static final String MOD_ID = "growitems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info(MOD_ID + " mod initialized");
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerModBlockEntities();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ItemGrowerBlockGrowthTimings.init(server.getRecipeManager(), server.getRegistryManager());
            ItemGrowerBlockGrowthTimings.computeAll();
        });
    }
}
