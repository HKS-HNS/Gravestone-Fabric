package com.hks.hns.gravestone;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Objects;

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

    /**
     * Creates a BlockWorldPos object from a given BlockPos and World.
     *
     * @param pos   the BlockPos to use
     * @param world the World to use
     */
    public BlockWorldPos(BlockPos pos, World world) {
        this(pos.getX(), pos.getY(), pos.getZ(), world);
    }

    /**
     * Returns a BlockPos representing this BlockWorldPos.
     *
     * @return a BlockPos representing this BlockWorldPos
     */
    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    /**
     * Returns a Vec3i representing this BlockWorldPos.
     *
     * @return a Vec3i representing this BlockWorldPos
     */
    public Vec3i getVec3i() {
        return new Vec3i(x, y, z);
    }

    /**
     * Returns the World that this BlockWorldPos is in.
     *
     * @return the World that this BlockWorldPos is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * Sets the World that this BlockWorldPos is in.
     *
     * @param world the new World to use
     */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Returns the x coordinate of this BlockWorldPos.
     *
     * @return the x coordinate of this BlockWorldPos
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y coordinate of this BlockWorldPos.
     *
     * @return the y coordinate of this BlockWorldPos
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the z coordinate of this BlockWorldPos.
     *
     * @return the z coordinate of this BlockWorldPos
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the position of this BlockWorldPos to the given x, y, and z coordinates.
     *
     * @param x the new x coordinate
     * @param y the new y coordinate
     * @param z the new z coordinate
     */
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Sets the position of this BlockWorldPos to the given BlockPos.
     *
     * @param pos the new BlockPos to use
     */
    public void setPos(BlockPos pos) {
        setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockWorldPos that)) return false;
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
