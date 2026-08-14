package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;

public class FogRenderer extends AtmosphericFogEnvironment {

    private float lastFogDistance = 1000;

    @Override
    public void setupFog(FogData data, Camera camera, ClientLevel world, float viewDistance, DeltaTracker tickCounter) {
        super.setupFog(data, camera, world, viewDistance, tickCounter);
        float distance = getFogDistance(world, camera.entity(), tickCounter.getGameTimeDeltaTicks());
        float blendDelta = getFogBlendingDelta(camera.entity());
        float density = Mth.clamp(VoidFog.config.fogDensity.get() / 100F, 0, 1);

        data.environmentalStart = Mth.lerp(blendDelta, getFogStart(distance, density), data.environmentalStart);
        data.environmentalEnd = Mth.lerp(blendDelta, getFogEnd(distance, density), data.environmentalEnd);
    }

    @Override
    public boolean isApplicable(FogType submersionType, Entity cameraEntity) {
        return VoidFog.config.enabled.get()
                && super.isApplicable(submersionType, cameraEntity)
                && !(cameraEntity instanceof LivingEntity l && l.hasEffect(MobEffects.BLINDNESS))
                && Voidable.of(cameraEntity.level()).hasDepthFog(cameraEntity, cameraEntity.level());
    }

    private float getFogDistance(ClientLevel world, Entity cameraEntity, float tickDelta) {
        Voidable voidable = Voidable.of(world);

        float viewDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
        double fogDistance = Luminance.getLuminance(cameraEntity, VoidFog.config.respectTorches.get()) / 16D
                           + getAltitude(voidable, world, cameraEntity) / (VoidFog.config.maxFogHeight.get() * getDifficultyMultiplier(world));
        float distance = fogDistance >= 1 ? viewDistance : (float)Mth.clamp(100 * Math.pow(Math.max(fogDistance, 0), 2), 5, viewDistance);

        if (cameraEntity instanceof LivingEntity l && l.hasEffect(MobEffects.NIGHT_VISION)) {
            distance *= 4 * GameRenderer.nightVisionScale(l, tickDelta);
        }

        distance = Mth.lerp(tickDelta / (distance > lastFogDistance ? 20 : 10), lastFogDistance, distance);
        lastFogDistance = distance;
        return distance;

    }

    private static float getFogStart(float distance, float density) {
        float factor = 0.55F * (1 - (distance - 5) / 127F);
        return distance * Math.max(0, factor) - (1 - density) * 9.9F;
    }

    private static float getFogEnd(float distance, float density) {
        return distance + (1 - density) * 9.9F;
    }

    public static float getFogBlendingDelta(Entity entity) {
        if (VoidFog.config.prettyFog.get()) {
            return 0;
        }
        float entityAltitude = (float)getAltitude(Voidable.of(entity.level()), entity.level(), entity);
        float fogTransitionDistance = Math.max(0, VoidFog.config.fogTransitionDistance.get());
        float maxFogAltitude = VoidFog.config.maxFogHeight.get() - fogTransitionDistance;
        return Mth.clamp((entityAltitude - maxFogAltitude) / fogTransitionDistance, 0, 1);
    }

    public static float getDifficultyMultiplier(Level world) {
        return (VoidFog.config.scaleWithDifficulty.get() ? world.getDifficulty().getId() + 1 : 1);
    }

    public static double getAltitude(Voidable voidable, Level world, Entity entity) {
        entity = getCorrectEntity(entity);
        return voidable.isVoidFogDisabled(entity, world) ? VoidFog.config.maxFogHeight.get() + 1 : (entity.getY() - world.getMinY());
    }

    public static Entity getCorrectEntity(Entity entity) {
        while (entity.isPassenger() && !entity.getBlockStateOn().isAir()) {
            entity = entity.getVehicle();
        }
        return entity;
    }

}
