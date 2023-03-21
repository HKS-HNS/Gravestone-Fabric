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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public abstract class OnDeath {

    private final HashMap<BlockPos, Inventory> playerInventories = Data.getPlayerInventory();

    private static BlockPos searchAir(BlockPos pos, int radius, World world) {
        // search nearest air block
        RegistryKey<DimensionType> dimension = world.getDimensionKey();
        if((dimension.equals(DimensionTypes.OVERWORLD) || dimension.equals(DimensionTypes.OVERWORLD_CAVES) )  && pos.getY() < -64 ||( dimension.equals(DimensionTypes.THE_NETHER) || dimension.equals(DimensionTypes.THE_END)) && pos.getY() < 0) {
            radius = 100;
        }
        BlockPos random = pos.add(radius, radius, radius);
        BlockPos nearest = random;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos2 = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockState state = world.getBlockState(pos2);
                    if (state.isAir()) {
                        if (new Vec3d(pos.getX(), pos.getY(), pos.getZ()).distanceTo(new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ())) < new Vec3d(pos.getX(), pos.getY(), pos.getZ()).distanceTo(new Vec3d(nearest.getX(), nearest.getY(), nearest.getZ()))) {
                            nearest = pos2;
                        }
                    }
                }
            }
        }

        if (nearest.equals(random)) {
            nearest = pos;
        }

        return nearest;
    }

    @Shadow
    public abstract void playerTick();

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
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
        }

        // Disable block drop
        if (!drop) {
            player.getInventory().clear();
        }
    }
}