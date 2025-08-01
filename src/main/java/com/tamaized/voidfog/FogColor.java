package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FogColor extends FogModifier {
    private double brightness;

    @Override
    public boolean isDarknessModifier() {
        return true;
    }

    @Override
    public void applyStartEndModifier(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter) {
    }

    @Override
    public boolean shouldApply(CameraSubmersionType submersionType, Entity cameraEntity) {
        return VoidFog.config.enabled.get()
                && submersionType == CameraSubmersionType.ATMOSPHERIC
                && Voidable.of(cameraEntity.getWorld()).hasDepthFog(cameraEntity, cameraEntity.getWorld())
                && !Voidable.of(cameraEntity.getWorld()).isVoidFogDisabled(cameraEntity, cameraEntity.getWorld());
    }

    @Override
    public float applyDarknessModifier(LivingEntity cameraEntity, float darkness, float tickProgress) {
        double prevBrightness = brightness;
        float light = FogRenderer.getLight(cameraEntity);
        brightness = light >= 1 ? darkness : (1 - light);
        return Math.max(darkness, (float)MathHelper.lerp(tickProgress / (brightness > prevBrightness ? 10 : 2), prevBrightness, brightness));
    }
}
