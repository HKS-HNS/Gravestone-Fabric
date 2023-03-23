package com.hks.hns.gravestone;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

public class BlockWorldPos  {
    double x;
    double y;
    double z;
    String world;

    public BlockWorldPos(double x, double y, double z, Identifier world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world.toString();
    }

    public BlockWorldPos(@NotNull BlockPos pos, Identifier world) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.world = world.toString();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    public Vec3i getVec3i() {
        return new Vec3i(x, y, z);
    }

    public Identifier getWorld() {
        return Identifier.tryParse(world);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setWorld(Identifier world) {
        this.world = world.toString();
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPos(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

}
