package com.tamaized.voidfog;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tamaized.voidfog.api.Voidable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class FogRenderer {

    private float lastFogDistance = 1000;

    public void render(Camera camera,  FogType type, float viewDistance, boolean thickFog) {

        if (!VoidFog.config.enabled) {
            return;
        }

        Entity entity = camera.getFocusedEntity();

        if (entity.hasVehicle()) {
            entity = entity.getRootVehicle();
        }

        World world = entity.getEntityWorld();
        Voidable voidable = (Voidable)world.getDimension();

        if (!voidable.hasDepthFog(entity, world)) {
            return;
        }

        float distance = getFogDistance(world, entity);

        float delta = MinecraftClient.getInstance().getTickDelta();

        distance = MathHelper.lerp(delta / (distance > lastFogDistance ? 20 : 2), lastFogDistance, distance);

        lastFogDistance = distance;

        RenderSystem.setShaderFogStart(getFogStart(distance, type, world, thickFog));
        RenderSystem.setShaderFogEnd(getFogEnd(distance, type, world, thickFog));
    }

    private int getLightLevelU(Entity entity) {
        if (VoidFog.config.respectTorches) {
            return entity.world.getLightLevel(entity.getBlockPos());
        }
        return entity.world.getLightLevel(LightType.SKY, entity.getBlockPos());
    }

    private double getLightLevelV(Voidable voidable, World world, Entity entity) {
        return voidable.isVoidFogDisabled(entity, world) ? 15 : (entity.getY() + 4);
    }

    private float getFogDistance(World world, Entity entity) {
        Voidable voidable = (Voidable)world.getDimension();

        float viewDistance = MinecraftClient.getInstance().gameRenderer.getViewDistance();
        double maxHeight = 32 * (world.getDifficulty().getId() + 1);
        double fogDistance = getLightLevelU(entity) / 16D
                           + getLightLevelV(voidable, world, entity) / maxHeight;

        if (fogDistance >= 1) {
            return viewDistance;
        }
        fogDistance = Math.pow(Math.max(fogDistance, 0), 2);

        return (float)MathHelper.clamp(100 * fogDistance, 5, viewDistance);
    }

    private float getFogStart(float intensity, FogType type, World world, boolean thickFog) {
        if (thickFog) {
            return intensity * 0.05F;
        }

        if (type == FogType.FOG_SKY) {
            return 0;
        }

        return intensity * 0.75F;
    }

    private float getFogEnd(float intensity, FogType type, World world, boolean thickFog) {
        if (thickFog) {
            return Math.min(intensity, 192) / 2F;
        }

        return intensity;
    }
}
