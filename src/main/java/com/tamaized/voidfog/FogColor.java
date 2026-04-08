package com.tamaized.voidfog;

import org.jspecify.annotations.Nullable;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FogType;

public class FogColor extends FogEnvironment {
    private double brightness;

    @Override
    public boolean modifiesDarkness() {
        return true;
    }

    @Override
    public void setupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker) {
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity cameraEntity) {
        return VoidFog.config.enabled.get()
                && fogType == FogType.ATMOSPHERIC
                && Voidable.of(cameraEntity.level()).hasDepthFog(cameraEntity, cameraEntity.level())
                && !Voidable.of(cameraEntity.level()).isVoidFogDisabled(cameraEntity, cameraEntity.level());
    }

    @Override
    public float getModifiedDarkness(LivingEntity cameraEntity, float darkness, float tickProgress) {
        double prevBrightness = brightness;
        BlockPos pos = BlockPos.containing(FogRenderer.getCorrectEntity(cameraEntity).getEyePosition());
        float light = Math.max(0, cameraEntity.level().getBrightness(LightLayer.SKY, pos) - cameraEntity.level().getSkyDarken());
        brightness = light >= 1 ? darkness : (1 - light);
        return Math.max(darkness, (float)Mth.lerp(tickProgress / (brightness > prevBrightness ? 10 : 2), prevBrightness, brightness));
    }
}
