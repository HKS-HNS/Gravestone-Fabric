package com.hks.hns.gravestone.client.Events;

import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.config.Data;
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

import static com.hks.hns.gravestone.config.Data.savePlayerInventory;

@Environment(EnvType.SERVER)
@Mixin(Block.class)
public class OnBreak {
    private static final HashMap <BlockWorldPos, Inventory > playerInventory = Data.getPlayerInventory();
    @Inject(at = @At("HEAD"), method = "onBroken")
    public void onBlockBreak(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci) {
        dropItems(world, pos, 1);
    }
    //on Explode
    @Inject(at = @At("HEAD"), method = "onDestroyedByExplosion")
    public void onBlockExplode(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        dropItems(world, pos, 1);
    }

    //replace
    @Inject(at = @At("HEAD"), method = "replace(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V")
    private static void onBlockReplace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        if (state.getBlock() != newState.getBlock()) {
            dropItems(world, pos, 0);
        }
    }

    private static void dropItems(WorldAccess world, BlockPos pos, int up) {
        for (BlockWorldPos key: playerInventory.keySet()) {
            BlockPos keyPos = key.getBlockPos();
            if (keyPos.equals(pos) || keyPos.equals(pos.up(up))) {
                System.out.println(keyPos);
                // Drop inventory
                boolean didDrop = false;
                Inventory inventory = playerInventory.get(key);
                for (int i = 0; i < inventory.size(); i++) {
                    //spawn item in middle of the block
                    ItemEntity itemEntity = new ItemEntity((World) world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory.getStack(i));
                    world.spawnEntity(itemEntity);
                    didDrop = true;
                }
                if (didDrop) {
                    System.out.println("Dropped items");
                }
                // Remove from hashmap
                playerInventory.remove(key);
                savePlayerInventory();
            }
        }
    }

}