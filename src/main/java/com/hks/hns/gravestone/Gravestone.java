package com.hks.hns.gravestone;

import net.fabricmc.api.ModInitializer;

import static com.hks.hns.gravestone.Data.loadPlayerInventory;

public class Gravestone implements ModInitializer {
    @Override
    public void onInitialize() {
        loadPlayerInventory();
    }
}
