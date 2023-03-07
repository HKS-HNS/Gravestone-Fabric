package com.hks.hns.gravestone;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class Data {
    static HashMap < BlockPos, Inventory > playerInventory = new HashMap < > ();
    public static HashMap < BlockPos, Inventory > getPlayerInventory() {
        return playerInventory;
    }

}