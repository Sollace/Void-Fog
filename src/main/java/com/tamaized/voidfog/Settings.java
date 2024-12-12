package com.tamaized.voidfog;

import java.nio.file.Path;
import com.minelittlepony.common.util.settings.Config;
import com.minelittlepony.common.util.settings.Setting;

public class Settings extends Config {
	public final Setting<Boolean> enabled = value("enabled", true);
	public final Setting<Boolean> scaleWithDifficulty = value("scaleWithDifficulty", true)
	        .addComment("Default: true")
	        .addComment("Makes fog start at shallower depths as difficulty increases");
	public final Setting<Boolean> disableInCreative = value("disableInCreative", true)
	        .addComment("Default: true")
            .addComment("Disables all void fog effects when in creative mode");
	public final Setting<Boolean> respectTorches = value("respectTorches", true)
	        .addComment("Default: true")
            .addComment("Fog will retreat from areas lit by torches and other light sources");
	public final Setting<Boolean> prettyFog = value("prettyFog", false)
	        .addComment("Default: false")
            .addComment("Enables an alternate method of rendering fog that gives softed edges that some may find more visually pleasing");
	public final Setting<Integer> voidParticleDensity = value("voidParticleDensity", 1000)
            .addComment("Default: 1000")
            .addComment("The particle density when near the void");
	public final Setting<Integer> fogDensity = value("fogDensity", 100)
	        .addComment("Default: 100")
	        .addComment("Min: 0, Max: 1")
            .addComment("The percentage particle density not near the void. voidParticleDensity is MULTIPLIED by this value/100");
	public final Setting<Integer> maxFogHeight = value("maxFogHeight", 32)
	        .addComment("Default: 32")
            .addComment("Maximum height in blocks from the bottom of the world that fog will reach.")
	        .addComment("If scaleWithDifficulty is enabled, this value is MULTIPLIED by the current area's difficulty");
	public final Setting<Float> fogTransitionDistance = value("fogTransitionDistance", 15F)
            .addComment("Default: 15")
            .addComment("Distance in blocks that you have to approach to the border defined by maxFogHeight for the game to start transitioning from regular to void fog");
	public final Setting<Boolean> imABigBoi = value("imABigBoi", false)
	        .addComment("Default: false")
            .addComment("Adds minimal jumpscares hanging around in dark areas for long amounts of time.");

    protected Settings(Path path) {
        super(HEIRARCHICAL_JSON_ADAPTER, path);
    }

	public float setParticleDensity(float density) {
	    density = density > 9997 ? 10000 : density < 3 ? 0 : density;

	    if (Math.abs(density - 1000) < 30) {
	        density = 1000;
	    }

	    return voidParticleDensity.set(Math.max(0, (int)density));
	}

	public float setFogHeight(float height) {
		return maxFogHeight.set((int)height);
	}

	public float setFogDensity(float density) {
		density = density > 97 ? 100 : density < 3 ? 0 : density;
		return fogDensity.set((int)density);
	}

	public float setFadeStart(float value) {
		return fogTransitionDistance.set(Math.max(0, value));
	}
}
