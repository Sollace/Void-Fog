package com.tamaized.voidfog;

import org.jetbrains.annotations.Range;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;

public interface Luminance {
    @Range(from = 0, to = 15)
    static int getLuminance(Entity entity, boolean includeBlocks) {
        entity = FogRenderer.getCorrectEntity(entity);
        BlockPos pos = BlockPos.containing(entity.getEyePosition());
        // LambDynamicLights changes the value returned here, so we do it this way rather than using Level#getBrightness to maintain compatibility
        int lightCoords = LevelRenderer.getLightCoords(entity.level(), pos);

        if (includeBlocks) {
            return Math.max(LightCoordsUtil.block(lightCoords), LightCoordsUtil.sky(lightCoords));
        }
        return LightCoordsUtil.sky(lightCoords);
    }
}
