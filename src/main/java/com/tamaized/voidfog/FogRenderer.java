package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class FogRenderer {

    private float lastFogDistance = 1000;

    public Fog render(Fog fog, Camera camera, FogType type, float viewDistance, boolean thickFog, float tickDelta) {

        if (!canRenderDepthFog(camera)) {
            return fog;
        }

        Entity entity = camera.getFocusedEntity();
        World world = entity.getEntityWorld();
        Voidable voidable = Voidable.of(world);

        if (!voidable.hasDepthFog(entity, world)) {
            return fog;
        }

        float distance = getFogDistance(world, entity);

        if (entity instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            distance *= 4 * GameRenderer.getNightVisionStrength(l, tickDelta);
        }

        distance = MathHelper.lerp(tickDelta / (distance > lastFogDistance ? 20 : 10), lastFogDistance, distance);
        lastFogDistance = distance;

        float blendDelta = getFogBlendingDelta(entity);
        float density = MathHelper.clamp(VoidFog.config.fogDensity.get() / 100F, 0, 1);

        float darkenAmount = VoidFog.FOG_COLOR.getFogBrightness((ClientWorld)world, camera.getFocusedEntity(), tickDelta);

        float start = MathHelper.lerp(blendDelta, getFogStart(distance, density, type, world, thickFog), fog.start());
        float end = MathHelper.lerp(blendDelta, getFogEnd(distance, density, type, world, thickFog), fog.end());

        return new Fog(start, end, fog.shape(), fog.red() * darkenAmount, fog.green() * darkenAmount, fog.blue() * darkenAmount, fog.alpha());
    }

    private boolean canRenderDepthFog(Camera camera) {
        return VoidFog.config.enabled.get()
                && camera.getSubmersionType() == CameraSubmersionType.NONE
                && !(camera.getFocusedEntity() instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.BLINDNESS));
    }

    public static float getFogBlendingDelta(Entity entity) {
        if (VoidFog.config.prettyFog.get()) {
            return 0;
        }
        float entityAltitude = (float)getAltitude(Voidable.of(entity.getWorld()), entity.getWorld(), entity);
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
            return entity.getWorld().getLightLevel(pos);
        }
        return entity.getWorld().getLightLevel(LightType.SKY, pos);
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

    private float getFogDistance(World world, Entity entity) {
        Voidable voidable = Voidable.of(world);

        float viewDistance = MinecraftClient.getInstance().gameRenderer.getViewDistanceBlocks();
        double fogDistance = getLight(entity) / 16D
                           + getAltitude(voidable, world, entity) / (VoidFog.config.maxFogHeight.get() * getDifficultyMultiplier(world));

        if (fogDistance >= 1) {
            return viewDistance;
        }
        fogDistance = Math.pow(Math.max(fogDistance, 0), 2);

        return (float)MathHelper.clamp(100 * fogDistance, 5, viewDistance);
    }

    private float getFogStart(float distance, float density, FogType type, World world, boolean thickFog) {
        if (type == FogType.FOG_SKY) {
            return 0;
        }

        if (thickFog) {
            return distance * 0.05F;
        }

        float factor = 0.55F * (1 - (distance - 5) / 127F);

        return distance * Math.max(0, factor) - (1 - density) * 9.9F;
    }

    private float getFogEnd(float distance, float density, FogType type, World world, boolean thickFog) {
        return thickFog ? Math.min(distance, 192) / 2F : distance + (1 - density) * 9.9F;
    }
}
