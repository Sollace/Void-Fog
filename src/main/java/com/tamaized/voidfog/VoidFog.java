package com.tamaized.voidfog;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.common.util.GamePaths;
import com.tamaized.voidfog.api.Voidable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.entity.Entity;

import static net.minecraft.client.render.fog.FogRenderer.*;

public class VoidFog implements ClientModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("voidfog");

	public static final FogParticleSpawner PARTICLE_SPAWNER = new FogParticleSpawner();
	public static final FogColor FOG_COLOR = new FogColor();
	public static final FogRenderer RENDERER = new FogRenderer();
	public static final InsanityEngine INSANITY = new InsanityEngine();

	public static Settings config;

    @Override
    public void onInitializeClient() {
        config = new Settings(GamePaths.getConfigDirectory().resolve("voidfog.json"));
        config.load();
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        FOG_MODIFIERS = new ArrayList<>(FOG_MODIFIERS);
        var atmospheric = FOG_MODIFIERS.stream().filter(i -> i instanceof AtmosphericFogModifier).toList();
        if (atmospheric.isEmpty()) {
            FOG_MODIFIERS.add(RENDERER);
        } else {
            FOG_MODIFIERS.add(FOG_MODIFIERS.indexOf(atmospheric.getLast()), RENDERER);
        }
        FOG_MODIFIERS.add(FOG_COLOR);
    }

    private void onTick(MinecraftClient client) {
        if (!config.enabled.get() || client.isPaused() || client.world == null || client.getCameraEntity() == null) {
            return;
        }

        Voidable dimension = Voidable.of(client.world);

        Entity entity = client.getCameraEntity();

        if (!dimension.hasDepthFog(entity, client.world)) {
            return;
        }

        PARTICLE_SPAWNER.update(client.world, entity, dimension);

        if (config.imABigBoi.get()) {
            INSANITY.update(client.world, entity, dimension);
        }
    }
}
