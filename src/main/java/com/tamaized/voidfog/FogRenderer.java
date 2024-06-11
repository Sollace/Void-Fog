package com.tamaized.voidfog;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tamaized.voidfog.api.Voidable;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
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

    public void render(Camera camera,  FogType type, float viewDistance, boolean thickFog, float delta) {

        if (!canRenderDepthFog(camera)) {
            return;
        }

        Entity entity = camera.getFocusedEntity();
        World world = entity.getEntityWorld();
        Voidable voidable = Voidable.of(world);

        if (!voidable.hasDepthFog(entity, world)) {
            return;
        }

        float distance = getFogDistance(world, entity);

        if (entity instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            distance *= 4 * GameRenderer.getNightVisionStrength(l, delta);
        }

        distance = MathHelper.lerp(delta / (distance > lastFogDistance ? 20 : 10), lastFogDistance, distance);
        lastFogDistance = distance;

        float blendDelta = getFogBlendingDelta(entity);
        float density = MathHelper.clamp(VoidFog.config.fogDensity / 100F, 0, 1);

        RenderSystem.setShaderFogStart(MathHelper.lerp(blendDelta, getFogStart(distance, density, type, world, thickFog), RenderSystem.getShaderFogStart()));
        RenderSystem.setShaderFogEnd(MathHelper.lerp(blendDelta, getFogEnd(distance, density, type, world, thickFog), RenderSystem.getShaderFogEnd()));
    }

    private boolean canRenderDepthFog(Camera camera) {
        return VoidFog.config.enabled
                && camera.getSubmersionType() == CameraSubmersionType.NONE
                && !(camera.getFocusedEntity() instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.BLINDNESS));
    }

    public static float getFogBlendingDelta(Entity entity) {
        if (VoidFog.config.prettyFog) {
            return 0;
        }
        float entityAltitude = (float)getAltitude(Voidable.of(entity.getWorld()), entity.getWorld(), entity);
        float fogHeight = VoidFog.config.fadeStartOffset;
        float maxFogAltitude = VoidFog.config.maxFogHeight - fogHeight;
        return MathHelper.clamp((entityAltitude - maxFogAltitude) / fogHeight, 0, 1);
    }

    public static float getDifficultyMultiplier(World world) {
        return (VoidFog.config.scaleWithDifficulty ? world.getDifficulty().getId() + 1 : 1);
    }

    public static int getLight(Entity entity) {
        entity = getCorrectEntity(entity);
        BlockPos pos = BlockPos.ofFloored(entity.getEyePos());
        if (VoidFog.config.respectTorches) {
            return entity.getWorld().getLightLevel(pos);
        }
        return entity.getWorld().getLightLevel(LightType.SKY, pos);
    }

    private static double getAltitude(Voidable voidable, World world, Entity entity) {
        entity = getCorrectEntity(entity);
        return voidable.isVoidFogDisabled(entity, world) ? 15 : (entity.getY() - world.getBottomY());
    }

    public static Entity getCorrectEntity(Entity entity) {
        while (entity.hasVehicle() && !entity.getBlockStateAtPos().isAir()) {
            entity = entity.getVehicle();
        }
        return entity;
    }

    private float getFogDistance(World world, Entity entity) {
        Voidable voidable = Voidable.of(world);

        float viewDistance = MinecraftClient.getInstance().gameRenderer.getViewDistance();
        double fogDistance = getLight(entity) / 16D
                           + getAltitude(voidable, world, entity) / (VoidFog.config.maxFogHeight * getDifficultyMultiplier(world));

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
