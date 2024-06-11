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

        distance = MathHelper.lerp(delta / (distance > lastFogDistance ? 20 : 2), lastFogDistance, distance);
        lastFogDistance = distance;

        float entityAltitude = (float)getAltitude(voidable, world, entity);
        int fadeStart = VoidFog.config.maxFogHeight;
        float fadeOffset = VoidFog.config.fadeStartOffset;
        float fadeEnd = fadeStart - fadeOffset;
        float entityDelta = Math.max(0, Math.min(1, (1 - (entityAltitude - fadeEnd) / fadeOffset)));

        if (entityAltitude <= fadeEnd || VoidFog.config.prettyFog) {
            RenderSystem.setShaderFogStart(getFogStart(distance, type, world, thickFog));
            RenderSystem.setShaderFogEnd(getFogEnd(distance, type, world, thickFog));
        } else if (entityAltitude <= fadeStart && !VoidFog.config.prettyFog) {
            RenderSystem.setShaderFogStart(MathHelper.lerp(entityDelta, RenderSystem.getShaderFogStart(), getFogStart(distance, type, world, thickFog)));
            RenderSystem.setShaderFogEnd(MathHelper.lerp(entityDelta, RenderSystem.getShaderFogEnd(), getFogEnd(distance, type, world, thickFog)));
        } else {
            RenderSystem.setShaderFogStart(RenderSystem.getShaderFogStart());
            RenderSystem.setShaderFogEnd(RenderSystem.getShaderFogEnd());
        }
    }

    private boolean canRenderDepthFog(Camera camera) {
        return VoidFog.config.enabled
                && camera.getSubmersionType() == CameraSubmersionType.NONE
                && !(camera.getFocusedEntity() instanceof LivingEntity l && l.hasStatusEffect(StatusEffects.BLINDNESS));
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
        double maxHeight = (VoidFog.config.scaleWithDifficulty) ? VoidFog.config.maxFogHeight * (world.getDifficulty().getId() + 1)
                                                                : VoidFog.config.maxFogHeight;
        double fogDistance = getLight(entity) / 16D
                           + getAltitude(voidable, world, entity) / maxHeight;

        if (fogDistance >= 1) {
            return viewDistance;
        }
        fogDistance = Math.pow(Math.max(fogDistance, 0), 2);

        return (float)MathHelper.clamp(100 * fogDistance, 5, viewDistance);
    }

    private float getFogStart(float distance, FogType type, World world, boolean thickFog) {
        if (type == FogType.FOG_SKY) {
            return 0;
        }

        if (thickFog) {
            return distance * 0.05F;
        }

        float factor = 0.55F * (1 - (distance - 5) / 127F);

        return distance * Math.max(0, factor);
    }

    private float getFogEnd(float distance, FogType type, World world, boolean thickFog) {
        return thickFog ? Math.min(distance, 192) / 2F : distance;
    }
}
