package com.tamaized.voidfog.api;

import com.tamaized.voidfog.VoidFog;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    default boolean isNearBedrock(BlockPos pos, Level world) {
        return pos.getY() < world.getMinY() + 6;
    }

    default boolean hasInsanity(BlockPos pos, Level world) {
        return pos.getY() <= 10 || world.isDarkOutside();
    }

    default boolean hasDepthFog(Entity entity, Level world) {

        if (entity.isSpectator() || (
                   VoidFog.config.disableInCreative.get()
                && entity instanceof Player p
                && p.isCreative())) {
            return false;
        }

        return world.isClientSide()
            && ((ClientLevel)world).getLevelData().getHorizonHeight(world) > world.getMinY()
            && world.dimensionType().hasSkyLight()
            && !world.dimensionType().hasCeiling();
    }

    default boolean isVoidFogDisabled(Entity player, Level world) {
        return !hasDepthFog(player, world);
    }

    static Voidable of(Level world) {
        if (world instanceof Voidable) {
            return (Voidable)world;
        }
        return EMPTY;
    }
}
