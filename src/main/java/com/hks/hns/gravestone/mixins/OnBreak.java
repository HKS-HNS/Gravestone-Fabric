package com.hks.hns.gravestone.mixins;

import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.config.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.hks.hns.gravestone.config.Data.savePlayerInventory;

@Environment(EnvType.SERVER)
@Mixin(Block.class)
public class OnBreak {

    // A map to store player inventory for each gravestone
    @Unique
    private static final ConcurrentHashMap<BlockWorldPos, Inventory> playerInventory = Data.getPlayerInventory();

    // Method to drop items from the player inventory when a block is replaced
    @Inject(at = @At("HEAD"), method = "replace(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V")
    private static void onBlockReplace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        if (state.getBlock() != newState.getBlock()) {
            closeInventories(pos, (ServerWorld) world);
            dropItems(world, pos, 0);
        }
    }

    // Method to drop items from the player inventory when a block is broken
    @Inject(at = @At("HEAD"), method = "onBroken")
    public void onBlockBreak(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci) {
        closeInventories(pos, (ServerWorld) world);
        dropItems(world, pos, 1);
    }

    // Method to drop items from the player inventory when a block is destroyed by explosion
    @Inject(at = @At("HEAD"), method = "onDestroyedByExplosion")
    public void onBlockExplode(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        closeInventories(pos, (ServerWorld) world);
        dropItems(world, pos, 1);
    }

    // Method to drop items from a gravestone's inventory and remove the inventory from the playerInventory map
    @Unique
    private static void dropItems(WorldAccess world, BlockPos pos, int up) {
        List<BlockWorldPos> keysToRemove = new ArrayList<>();

        for (BlockWorldPos key : playerInventory.keySet()) {
            BlockPos keyPos = key.getBlockPos();

            if (keyPos.equals(pos) || keyPos.equals(pos.up(up))) {

                // Drop inventory
                Inventory inventory = playerInventory.get(key);

                for (int i = 0; i < inventory.size(); i++) {
                    // Spawn item in the middle of the block
                    ItemEntity itemEntity = new ItemEntity((World) world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory.getStack(i));
                    world.spawnEntity(itemEntity);
                }


                // Remove from hashmap
                keysToRemove.add(key);
                savePlayerInventory();
            }

        }

        for (BlockWorldPos key : keysToRemove) {
            playerInventory.remove(key);
        }
    }

    @Unique
    private static void closeInventories(BlockPos pos, ServerWorld world) {
        BlockWorldPos blockWorldPos = new BlockWorldPos(pos, world);

        if(playerInventory.containsKey(blockWorldPos)) {
            Inventory inventory = playerInventory.get(blockWorldPos);

            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.currentScreenHandler instanceof GenericContainerScreenHandler container && container.getInventory() == inventory) {
                        player.closeHandledScreen();

                }
            }
        }
    }

}
