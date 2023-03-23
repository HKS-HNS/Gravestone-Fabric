package com.hks.hns.gravestone.client.Events;

import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.config.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hks.hns.gravestone.config.Data.savePlayerInventory;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerInteractionManager.class)
public class BlockInteract {
    private static final List<Integer> containerId = new ArrayList<>();

    private final HashMap<BlockWorldPos, Inventory> playerInventory = Data.getPlayerInventory();

    private static int getNextContainerId(PlayerEntity player) {
        // Find the index of the player's container ID in the containerId list
        int playerIndex = containerId.indexOf(player.getUuid().hashCode());

        if (playerIndex == -1) {
            // Player has no existing container ID, so create a new one
            containerId.add(player.getUuid().hashCode());
            containerId.add(1);
            return player.currentScreenHandler.syncId + 1;
        } else {
            // Player has an existing container ID, so increment it
            int nextContainerId = containerId.get(playerIndex + 1) + 1;
            containerId.set(playerIndex + 1, nextContainerId);
            return player.currentScreenHandler.syncId + nextContainerId;
        }
    }

    @Inject(at = @At("HEAD"), method = "interactBlock")
    public void interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        for (BlockWorldPos pos : playerInventory.keySet()) {
            if (isEmpty(playerInventory.get(pos))) {
                playerInventory.remove(pos);
                savePlayerInventory();
            }
        }

        // Test if click is right click
        BlockPos pos = hitResult.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        BlockWorldPos blockWorldPos = new BlockWorldPos(pos, world.getRegistryKey().getValue());
        if (block == null) {
            return;
        }
        if (block.getDefaultState().getBlock() == Blocks.OAK_SIGN) {
            System.out.println("Sign clicked");
            if (playerInventory.containsKey(blockWorldPos)) {
                System.out.println("Gravestone found");
                int syncId = getNextContainerId(player);

                NamedScreenHandlerFactory containerProvider = new SimpleNamedScreenHandlerFactory((Inv, Player, In) -> GenericContainerScreenHandler.createGeneric9x6(syncId, player.getInventory(), playerInventory.get(blockWorldPos)), Text.of("Gravestone"));

                player.openHandledScreen(containerProvider);
            }
        }
    }

    public boolean isEmpty(Inventory inventory) {
        if (inventory == null) {
            return true;
        }

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i) != null && !inventory.getStack(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}