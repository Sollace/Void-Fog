package com.tamaized.voidfog;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.common.util.GamePaths;
import com.tamaized.voidfog.api.Voidable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.world.entity.Entity;

import static net.minecraft.client.renderer.fog.FogRenderer.FOG_ENVIRONMENTS;

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

        FOG_ENVIRONMENTS = new ArrayList<>(FOG_ENVIRONMENTS);
        var atmospheric = FOG_ENVIRONMENTS.stream().filter(i -> i instanceof AtmosphericFogEnvironment).toList();
        if (atmospheric.isEmpty()) {
            FOG_ENVIRONMENTS.add(RENDERER);
        } else {
            FOG_ENVIRONMENTS.add(FOG_ENVIRONMENTS.indexOf(atmospheric.getLast()), RENDERER);
        }
        FOG_ENVIRONMENTS.add(FOG_COLOR);
    }

    private void onTick(Minecraft client) {
        if (!config.enabled.get() || client.isPaused() || client.level == null || client.getCameraEntity() == null) {
            return;
        }

        Voidable dimension = Voidable.of(client.level);

        Entity entity = client.getCameraEntity();

        if (!dimension.hasDepthFog(entity, client.level)) {
            return;
        }

        PARTICLE_SPAWNER.update(client.level, entity, dimension);

        if (config.imABigBoi.get()) {
            INSANITY.update(client.level, entity, dimension);
        }
    }
}
