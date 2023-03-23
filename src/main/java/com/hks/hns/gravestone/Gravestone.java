package com.hks.hns.gravestone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import static com.hks.hns.gravestone.config.Data.loadPlayerInventory;

public class Gravestone implements ModInitializer {
    private static MinecraftServer server ;
    // get server instance
    @Override
    public void onInitialize() {
        loadPlayerInventory();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Gravestone.server = server;});
    }

    public static MinecraftServer getServer() {
        return server;
    }


}
