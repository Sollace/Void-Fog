package com.tamaized.voidfog.api;

import com.tamaized.voidfog.VoidFog;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * These are the defaults.
 *
 * You can choose to implement this interface and override any of these methods
 * to change how Void Fog interacts with your modded dimension.
 */
public interface Voidable {
    Voidable EMPTY = new Voidable() {};

    default int getDepthParticleRate(BlockPos pos) {
        return pos.getY();
    }

    default boolean isNearBedrock(BlockPos pos, World world) {
        return pos.getY() < world.getBottomY() + 6;
    }

    default boolean hasInsanity(BlockPos pos, World world) {
        return pos.getY() <= 10 || world.isNight();
    }

    default boolean hasDepthFog(Entity entity, World world) {

        if (entity.isSpectator() || (
                   VoidFog.config.disableInCreative
                && entity instanceof PlayerEntity p
                && p.isCreative())) {
            return false;
        }

        return world.isClient
            && ((ClientWorld)world).getLevelProperties().getSkyDarknessHeight(world) != 0
            && world.getDimension().hasSkyLight()
            && !world.getDimension().hasCeiling();
    }

    default boolean isVoidFogDisabled(Entity player, World world) {
        return !hasDepthFog(player, world);
    }

    static Voidable of(World world) {
        if (world instanceof Voidable) {
            return (Voidable)world;
        }
        return EMPTY;
    }
}
