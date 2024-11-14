package com.tamaized.voidfog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.common.util.GamePaths;
import com.tamaized.voidfog.api.Voidable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

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
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
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
