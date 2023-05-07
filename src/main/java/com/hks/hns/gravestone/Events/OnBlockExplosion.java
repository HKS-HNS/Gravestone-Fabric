package com.hks.hns.gravestone.Events;

import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.config.Data;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.SERVER)
@Mixin(Explosion.class)
public class OnBlockExplosion {

    private static final ConcurrentHashMap<BlockWorldPos, Inventory> playerInventory = Data.getPlayerInventory();
    @Shadow
    @Final
    private ObjectArrayList<BlockPos> affectedBlocks;
    @Shadow
    @Final
    private World world;

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/explosion/Explosion;affectWorld(Z)V")
    private void onAffectWorld(boolean particles, CallbackInfo ci) {
        for (BlockWorldPos key : playerInventory.keySet()) {
            BlockPos pos2 = key.getBlockPos();
            affectedBlocks.remove(pos2);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos pos = pos2.add(dx, dy, dz);
                        if (affectedBlocks.contains(pos)) {
                            final BlockState state = world.getBlockState(pos2);
                            world.setBlockState(pos2, Blocks.AIR.getDefaultState());
                            world.setBlockState(pos, Blocks.AIR.getDefaultState());
                            world.setBlockState(pos2, state);
                            affectedBlocks.remove(pos);
                        }
                    }
                }
            }

        }
    }
}
