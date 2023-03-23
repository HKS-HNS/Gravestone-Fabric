package com.hks.hns.gravestone;

import java.util.Objects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BlockWorldPos {
    double x;
    double y;
    double z;
    World world;

    public BlockWorldPos(double x, double y, double z, World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public BlockWorldPos(BlockPos pos, World world) {
        this(pos.getX(), pos.getY(), pos.getZ(), world);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    public Vec3i getVec3i() {
        return new Vec3i(x, y, z);
    }

    public World getWorld() {
        return world;
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

    public void setWorld(World world) {
        this.world = world;
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPos(BlockPos pos) {
        setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockWorldPos)) return false;
        BlockWorldPos that = (BlockWorldPos) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world);
    }
}
