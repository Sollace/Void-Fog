package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FogParticleSpawner {

    private static final int RADIUS = 16;

    private BlockPos randomPos(Random rand) {
        return new BlockPos(rand.nextInt(RADIUS), rand.nextInt(RADIUS), rand.nextInt(RADIUS));
    }

    public void update(World world, Entity entity, Voidable dimension) {

        if (FogRenderer.getAltitude(dimension, world, entity) > VoidFog.config.maxFogHeight) {
            return;
        }

        int particleCount = (int)(VoidFog.config.voidParticleDensity * (1 - FogRenderer.getFogBlendingDelta(entity)));
        int difficultyMultiplier = (int)(8 * FogRenderer.getDifficultyMultiplier(world));
        Random rand = world.getRandom();

        for (int pass = 0; pass < particleCount; pass++) {
            BlockPos pos = randomPos(rand).subtract(randomPos(rand)).add(entity.getBlockPos());
            BlockState state = world.getBlockState(pos);

            if (state.isAir()
                    && world.getFluidState(pos).isEmpty()
                    && (pos.getY() - world.getBottomY()) <= VoidFog.config.maxFogHeight
                    && rand.nextInt(difficultyMultiplier) <= VoidFog.config.maxFogHeight) {
                boolean nearBedrock = dimension.isNearBedrock(pos, world);

                world.addParticle(nearBedrock ? ParticleTypes.ASH : ParticleTypes.MYCELIUM,
                        pos.getX() + rand.nextFloat(),
                        pos.getY() + rand.nextFloat(),
                        pos.getZ() + rand.nextFloat(),
                        0,
                        nearBedrock ? rand.nextFloat() : 0,
                        0
                );
            }
        }
    }
}







