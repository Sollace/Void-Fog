package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FogParticleSpawner {

    private static final int RADIUS = 16;
    private static final int PARTICLE_INSET_HEIGHT = 9;

    private BlockPos randomPos(RandomSource rand) {
        return new BlockPos(rand.nextInt(RADIUS), rand.nextInt(RADIUS), rand.nextInt(RADIUS));
    }

    public void update(Level world, Entity entity, Voidable dimension) {

        int maxParticleHeight = VoidFog.config.maxFogHeight.get() - PARTICLE_INSET_HEIGHT;

        if (FogRenderer.getAltitude(dimension, world, entity) > maxParticleHeight) {
            return;
        }

        int particleCount = (int)(VoidFog.config.voidParticleDensity.get() * (1 - FogRenderer.getFogBlendingDelta(entity)));
        int difficultyMultiplier = (int)(8 * FogRenderer.getDifficultyMultiplier(world));
        RandomSource rand = world.getRandom();

        for (int pass = 0; pass < particleCount; pass++) {
            BlockPos pos = randomPos(rand).subtract(randomPos(rand)).offset(entity.blockPosition());
            BlockState state = world.getBlockState(pos);

            if (state.isAir()
                    && world.getFluidState(pos).isEmpty()
                    && (pos.getY() - world.getMinY()) <= maxParticleHeight
                    && rand.nextInt(difficultyMultiplier) <= maxParticleHeight) {
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







