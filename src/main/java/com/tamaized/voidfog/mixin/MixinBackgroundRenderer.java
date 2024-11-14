package com.tamaized.voidfog.mixin;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.tamaized.voidfog.VoidFog;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;

@Mixin(BackgroundRenderer.class)
abstract class MixinBackgroundRenderer {
    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void onApplyFog(Camera camera, FogType type, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> info) {
        info.setReturnValue(VoidFog.RENDERER.render(info.getReturnValue(), camera, type, viewDistance, thickenFog, tickDelta));
    }
}
