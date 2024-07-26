package com.hks.hns.gravestone.mixins;

import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.config.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.hks.hns.gravestone.config.Data.savePlayerInventory;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public abstract class OnDeath {

    // HashMap to store player inventories
    @Unique
    private final ConcurrentHashMap< BlockWorldPos, Inventory > playerInventories = Data.getPlayerInventory();

    // Helper method to check if the player is in the overworld
    @Unique
    private static boolean isOverWorld(World world) {
        RegistryKey < DimensionType > dimension = world.getDimensionEntry().getKey().orElseThrow();
        return dimension.equals(DimensionTypes.OVERWORLD) || dimension.equals(DimensionTypes.OVERWORLD_CAVES);
    }

    // Helper method to check if the player is underground
    @Unique
    private static boolean isUnderGround(World world, BlockPos pos) {
        RegistryKey < DimensionType > dimension = world.getDimensionEntry().getKey().orElseThrow();
        return (isOverWorld(world) && pos.getY() < -64) || (dimension.equals(DimensionTypes.THE_NETHER) || dimension.equals(DimensionTypes.THE_END)) && pos.getY() < 0;
    }

    // Helper method to search for the nearest air block
    @Unique
    private static BlockPos searchAir(BlockPos pos, int radius, World world) {
        // If in the overworld or overworld caves and below y level -64, search a larger radius around y level 0
        // If in the nether or end and below y level 0, search a larger radius around y level 0
        if (isUnderGround(world, pos)) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());

            if (isOverWorld(world)) {
                pos = new BlockPos(pos.getX(), -64, pos.getZ());
            }

            if (world.getBlockState(pos).isAir())
                return pos;
            radius = 20;
        }

        BlockPos random = pos.add(radius, radius, radius);
        BlockPos nearest = random;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos2 = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockState state = world.getBlockState(pos2);

                    if (state.isAir() && !isUnderGround(world, pos2)) {
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
    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onPlayerDeath(CallbackInfo ci) {

        ServerPlayerEntity player = (ServerPlayerEntity)(Object) this;
        World world = player.getWorld();
        BlockPos pos = player.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        BlockWorldPos worldPos = new BlockWorldPos(pos, world);

        // Clone player inventory
        PlayerInventory playerInventory = player.getInventory();
        boolean drop = false;

        if (!block.getDefaultState().isAir() && block.getDefaultState().getBlock() != Blocks.WATER || isUnderGround(world, pos)) {
            pos = searchAir(pos, 5, world);
            block = world.getBlockState(pos).getBlock();

        }

        if (!block.getDefaultState().isAir() && block.getDefaultState().getBlock() != Blocks.WATER) {
            drop = true;

        } else {
            world.setBlockState(pos, Blocks.OAK_SIGN.getDefaultState());

            Date time = new Date();
            Text[] signMessage = {Text.literal("RIP"), Text.literal(player.getName().getString()), Text.literal(new SimpleDateFormat("yyyy MM dd").format(time))};
            SignText signText = new SignText();

            for (int i = 0; i < signMessage.length; i++) {
                signText = signText.withMessage(i, signMessage[i]);

            }

            signText = signText.withGlowing(true);
            SignBlockEntity signBlockEntity = world.getBlockEntity(pos, BlockEntityType.SIGN).orElseThrow();
            signBlockEntity.setText(signText, true);
            signBlockEntity.setText(signText, false);
            signBlockEntity.setWaxed(true);
            world.updateListeners(pos, block.getDefaultState(), signBlockEntity.getCachedState(), 3);

            // Create inventory with 54 slots
            Inventory inventory = new SimpleInventory(54);
            for (int i = 0; i < playerInventory.size(); i++) {
                inventory.setStack(i, playerInventory.getStack(i));

            }

            worldPos.setPos(pos);
            playerInventories.put(worldPos, inventory);
            savePlayerInventory();

        }

        // Disable block drop
        if (!drop) {
            player.getInventory().clear();

        }
    }
}