package com.hks.hns.gravestone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import static com.hks.hns.gravestone.config.Data.loadPlayerInventory;

public class Gravestone implements ModInitializer {
    private static MinecraftServer server;

    // Getter for the server instance
    public static MinecraftServer getServer() {
        return server;
    }

    // Register a callback to be invoked when the server starts
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Store the server instance in a static field for easy access
            Gravestone.server = server;
            // Load the player inventories from the save file
            loadPlayerInventory();
        });
    }
}
