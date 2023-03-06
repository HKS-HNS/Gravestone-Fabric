package com.hks.hns.gravestone.client.Events;

import com.hks.hns.gravestone.client.GravestoneClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerInteractionManager.class)
public class BlockInteract{
    @Shadow @Final protected ServerPlayerEntity player;
    HashMap< Block, PlayerInventory> playerInventory = GravestoneClient.getPlayerInventory();

    @Inject(at = @At("HEAD"), method = "interactBlock")
    public void interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        for (Block block: playerInventory.keySet()) {
            if (isEmpty(playerInventory.get(block))) {
                playerInventory.remove(block);
            }
        }
        //test if click is right click
        if (hitResult.getType() == BlockHitResult.Type.BLOCK ) {
            return;
            world.getBlockState(hitResult.getBlockPos()).getBlock();
            Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
        if (block == null) return;
        if (block.getDefaultState().getBlock() == Blocks.OAK_SIGN) {
            if (playerInventory.containsKey(block)) {
                NamedScreenHandlerFactory screenHandlerFactory = GenericContainerScreenHandler.createGeneric9x6(ScreenHandlerRegistry., player.getInventory(), playerInventory.get(block));
            }
        }
    }}
    public boolean isEmpty(Inventory inventory) {
        if (inventory == null) {
            return true;
        }

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i) != null) {
                return false;
            }
        }
        return true;
    }
}
