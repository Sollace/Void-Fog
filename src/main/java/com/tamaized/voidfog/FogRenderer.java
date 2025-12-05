package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class FogRenderer extends AtmosphericFogModifier {

    private float lastFogDistance = 1000;

    @Override
    public void applyStartEndModifier(FogData data, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter) {
        super.applyStartEndModifier(data, camera, world, viewDistance, tickCounter);
        float distance = getFogDistance(world, camera.getFocusedEntity(), tickCounter.getDynamicDeltaTicks());
        float blendDelta = getFogBlendingDelta(camera.getFocusedEntity());
        float density = MathHelper.clamp(VoidFog.config.fogDensity.get() / 100F, 0, 1);

        data.environmentalStart = MathHelper.lerp(blendDelta, getFogStart(distance, density), data.environmentalStart);
        data.environmentalEnd = MathHelper.lerp(blendDelta, getFogEnd(distance, density), data.environmentalEnd);
    }

    @Override
    public boolean shouldApply(CameraSubmersionType submersionType, Entity cameraEntity) {
        return VoidFog.config.enabled.get()
                && super.shouldApply(submersionType, cameraEntity)
                && !(cameraEntity instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.BLINDNESS))
                && Voidable.of(cameraEntity.getEntityWorld()).hasDepthFog(cameraEntity, cameraEntity.getEntityWorld());
    }

    private float getFogDistance(ClientWorld world, Entity cameraEntity, float tickDelta) {
        Voidable voidable = Voidable.of(world);

        float viewDistance = MinecraftClient.getInstance().gameRenderer.getViewDistanceBlocks();
        double fogDistance = getLight(cameraEntity) / 16D
                           + getAltitude(voidable, world, cameraEntity) / (VoidFog.config.maxFogHeight.get() * getDifficultyMultiplier(world));
        float distance = fogDistance >= 1 ? viewDistance : (float)MathHelper.clamp(100 * Math.pow(Math.max(fogDistance, 0), 2), 5, viewDistance);

        if (cameraEntity instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            distance *= 4 * GameRenderer.getNightVisionStrength(l, tickDelta);
        }

        distance = MathHelper.lerp(tickDelta / (distance > lastFogDistance ? 20 : 10), lastFogDistance, distance);
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
        float entityAltitude = (float)getAltitude(Voidable.of(entity.getEntityWorld()), entity.getEntityWorld(), entity);
        float fogTransitionDistance = Math.max(0, VoidFog.config.fogTransitionDistance.get());
        float maxFogAltitude = VoidFog.config.maxFogHeight.get() - fogTransitionDistance;
        return MathHelper.clamp((entityAltitude - maxFogAltitude) / fogTransitionDistance, 0, 1);
    }

    public static float getDifficultyMultiplier(World world) {
        return (VoidFog.config.scaleWithDifficulty.get() ? world.getDifficulty().getId() + 1 : 1);
    }

    public static int getLight(Entity entity) {
        entity = getCorrectEntity(entity);
        BlockPos pos = BlockPos.ofFloored(entity.getEyePos());
        if (VoidFog.config.respectTorches.get()) {
            return entity.getEntityWorld().getLightLevel(pos);
        }
        return entity.getEntityWorld().getLightLevel(LightType.SKY, pos);
    }

    public static double getAltitude(Voidable voidable, World world, Entity entity) {
        entity = getCorrectEntity(entity);
        return voidable.isVoidFogDisabled(entity, world) ? VoidFog.config.maxFogHeight.get() + 1 : (entity.getY() - world.getBottomY());
    }

    public static Entity getCorrectEntity(Entity entity) {
        while (entity.hasVehicle() && !entity.getBlockStateAtPos().isAir()) {
            entity = entity.getVehicle();
        }
        return entity;
    }

}
