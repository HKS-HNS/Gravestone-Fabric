package com.hks.hns.gravestone.client.Events;

import com.hks.hns.gravestone.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Environment(EnvType.SERVER)
@Mixin(Block.class)
public class OnBreak {
    HashMap<BlockPos, Inventory> playerInventory = Data.getPlayerInventory();

    @Inject(at = @At("HEAD"), method = "onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
    public void onBlockBreak(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci) {

        dropItems(world, pos);
    }
    //on Explode
    @Inject(at = @At("HEAD"), method = "onDestroyedByExplosion")
    public void onBlockExplode(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        dropItems(world, pos);
    }

    //on Piston


    private void dropItems(WorldAccess world, BlockPos pos) {
        System.out.println("Block broken");
        for (BlockPos key : playerInventory.keySet()) {
            if (key.equals(pos) || key.equals(pos.down())) {
                // Drop inventory
                Inventory inventory = playerInventory.get(key);
                for (int i = 0; i < inventory.size(); i++) {
                    ItemEntity itemEntity = new ItemEntity(getWorld(world), pos.getX(), pos.getY(), pos.getZ(), inventory.getStack(i));
                    world.spawnEntity(itemEntity);
                }
                // Remove from hashmap
                playerInventory.remove(key);
                // Stop the block from breaking
            }
        }
    }

    public World getWorld(WorldAccess worldAccess) {
        if (worldAccess instanceof World) {
            return (World) worldAccess;
        } else {
            throw new IllegalArgumentException("Unsupported WorldAccess implementation");
        }
    }
}