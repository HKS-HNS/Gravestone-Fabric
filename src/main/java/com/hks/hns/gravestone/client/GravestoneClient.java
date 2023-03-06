package com.hks.hns.gravestone.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;

import java.util.HashMap;

@Environment(EnvType.SERVER)
public class GravestoneClient implements ClientModInitializer {
    static HashMap<Block, PlayerInventory> playerInventory = new HashMap < > ();

    @Override
    public void onInitializeClient() {

    }

    public static HashMap<Block, PlayerInventory> getPlayerInventory() {
        return playerInventory;
    }
}
