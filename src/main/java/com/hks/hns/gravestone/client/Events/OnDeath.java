package com.hks.hns.gravestone.client.Events;

import com.hks.hns.gravestone.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.HashMap;

import static com.hks.hns.gravestone.Data.savePlayerInventory;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public abstract class OnDeath {
    // HashMap to store player inventories
    private final HashMap<BlockPos, Inventory> playerInventories = Data.getPlayerInventory();

    // Helper method to check if the player is in the overworld
    private static boolean isOverWorld(World world) {
        RegistryKey<DimensionType> dimension = world.getDimensionKey();
        return dimension.equals(DimensionTypes.OVERWORLD) || dimension.equals(DimensionTypes.OVERWORLD_CAVES);
    }
    // Helper method to check if the player is underground
    private static boolean isUnderGround(World world, BlockPos pos) {
        RegistryKey<DimensionType> dimension = world.getDimensionKey();
        return (isOverWorld(world) && pos.getY() < -64 || (dimension.equals(DimensionTypes.THE_NETHER) || dimension.equals(DimensionTypes.THE_END)) && pos.getY() < 0);
    }

    // Helper method to search for the nearest air block
    private static BlockPos searchAir(BlockPos pos, int radius, World world) {

        // If in the overworld or overworld caves and below y level -64, search a larger radius around y level 0
        // If in the nether or end and below y level 0, search a larger radius around y level 0
        if (isUnderGround(world, pos)) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());

            if(isOverWorld(world))
                pos = new BlockPos(pos.getX(), -64, pos.getZ());

            radius = 100;
        }

        BlockPos random = pos.add(radius, radius, radius);
        BlockPos nearest = random;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos2 = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockState state = world.getBlockState(pos2);

                    if (state.isAir() && !isUnderGround(world, pos2)) {
                        // If the current position is an air block and closer to the player's position than the current nearest air block,
                        // update the nearest air block to the current position
                        if (new Vec3d(pos.getX(), pos.getY(), pos.getZ()).distanceTo(new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ())) < new Vec3d(pos.getX(), pos.getY(), pos.getZ()).distanceTo(new Vec3d(nearest.getX(), nearest.getY(), nearest.getZ()))) {
                            nearest = pos2;
                        }
                    }
                }
            }
        }

        // If no air block was found within the search radius, return the original position
        if (nearest.equals(random)) {
            nearest = pos;
        }

        return nearest;
    }

    @Inject(at = @At("HEAD"),
            method = "onDeath")
    public void onPlayerDeath(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        World world = player.getWorld();
        Block block = world.getBlockState(player.getBlockPos()).getBlock();
        BlockPos pos = player.getBlockPos();
        // Clone player inventory
        PlayerInventory playerInventory = player.getInventory();
        boolean drop = false;

        if (!block.getDefaultState().isAir() && block.getDefaultState().getBlock() != Blocks.WATER) {
            block = world.getBlockState(searchAir(pos, 5, world)).getBlock();
        }

        if (!block.getDefaultState().isAir() && block.getDefaultState().getBlock() != Blocks.WATER) {
            drop = true;
        } else {
            world.setBlockState(pos, Blocks.OAK_SIGN.getDefaultState());
            block = world.getBlockState(pos).getBlock();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            SignBlockEntity signBlockEntity = (SignBlockEntity) blockEntity;
            signBlockEntity.setTextOnRow(0, Text.of("RIP"));
            signBlockEntity.setTextOnRow(1, Text.of(player.getName().getString()));
            signBlockEntity.markDirty();
            world.updateListeners(pos, block.getDefaultState(), block.getDefaultState(), 3);

            // Create inventory with 54 slots
            Inventory inventory = new SimpleInventory(54);
            for (int i = 0; i < playerInventory.size(); i++) {
                inventory.setStack(i, playerInventory.getStack(i));
            }
            playerInventories.put(pos, inventory);
            savePlayerInventory();
        }

        // Disable block drop
        if (!drop) {
            player.getInventory().clear();
        }
    }
}