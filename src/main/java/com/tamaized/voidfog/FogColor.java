package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class FogColor {

    public double getFogBrightness(World world, Entity entity, float partialTick) {

        if (!VoidFog.config.enabled) {
            return 1;
        }

        Voidable voidable = (Voidable)world.getDimension();

        if (voidable.isVoidFogDisabled(entity, world)) {
            return 1;
        }

        double brightness = entity.prevY + (entity.y - entity.prevY) * partialTick * world.getDimension().getHorizonShadingRatio();

        if (brightness >= 1) {
            return 1;
        }

        brightness = Math.pow(Math.max(0, brightness), 2);

        return brightness;
    }
}
