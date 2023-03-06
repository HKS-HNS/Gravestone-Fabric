package com.hks.hns.gravestone.client.Events;

import com.hks.hns.gravestone.Gravestone;
import com.hks.hns.gravestone.client.GravestoneClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public abstract class onDeath {
    @Shadow public abstract void playerTick();

    HashMap<Block, PlayerInventory> playerInventories = GravestoneClient.getPlayerInventory();

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
    public void onPlayerDeath(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        World world = player.getWorld();
        Block block = world.getBlockState(player.getBlockPos()).getBlock();
        BlockPos pos = player.getBlockPos();
        //clone player inventory

        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clone(playerInventory);
        boolean drop = false;


        if (!block.getDefaultState().isAir() && block.getDefaultState().getBlock() != Blocks.WATER) {
           block = world.getBlockState(searchAir(pos, 5,world)).getBlock();
        }

        if (!block.getDefaultState().isAir() && block.getDefaultState().getBlock() != Blocks.WATER) {
            drop = true;

        } else {
            SignBlockEntity signBlockEntity = new SignBlockEntity(pos, block.getDefaultState());
            signBlockEntity.setTextOnRow(0, Text.of("RIP"));
            signBlockEntity.setTextOnRow(1, Text.of(player.getName().getString()));
            world.setBlockState(pos, signBlockEntity.getCachedState());
            playerInventories.put(block, playerInventory);

        }

        //disable block drop
        if (!drop) {
            player.getInventory().clear();
        }

    }
    private static BlockPos searchAir(BlockPos pos, int radius, World world) {
        // search nearest air block
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
}
