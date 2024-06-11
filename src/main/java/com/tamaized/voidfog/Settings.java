package com.tamaized.voidfog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class Settings {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

	public boolean enabled = true;

	public boolean scaleWithDifficulty = true;

	public boolean disableInCreative = true;

	public boolean respectTorches = true;

	public boolean prettyFog = false;

	public int voidParticleDensity = 1000;

	public int fogDensity = 100;

	public int maxFogHeight = 32;

	public float fadeStartOffset = 15F; //additive to maxFogHeight, not an absolute height.

	public boolean imABigBoi = false;

	private transient Path path;

	public float setParticleDensity(float density) {
	    density = density > 9997 ? 10000 : density < 3 ? 0 : density;

	    if (Math.abs(density - 1000) < 30) {
	        density = 1000;
	    }

	    voidParticleDensity = (int)density;

	    return voidParticleDensity;
	}

	public float setFogHeight(float height) {
		maxFogHeight = (int)height;
		return maxFogHeight;
	}

	public float setFogDensity(float density) {
		density = density > 97 ? 100 : density < 3 ? 0 : density;
		fogDensity = (int)density;
		return fogDensity;
	}

	public float setFadeStart(float value) {
		fadeStartOffset = value;
		return maxFogHeight;
	}

	public static Settings load(Path path) {
	    if (Files.isReadable(path)) {
            try (BufferedReader s = Files.newBufferedReader(path)) {
                Settings result = gson.fromJson(s, Settings.class);

                if (result != null) {
                    return result.save(path);
                }
            } catch (IOException | JsonParseException e) {
                VoidFog.LOGGER.warn("Erorr whilst loading json config", e);
            }
        }
	    return new Settings().save(path);
	}

	protected void validate() {
	    voidParticleDensity = Math.max(0, voidParticleDensity);
		fadeStartOffset = Math.max(0, fadeStartOffset);
	}

	private Settings save(Path path) {
	    this.path = path;
	    validate();
	    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            VoidFog.LOGGER.warn("Error whilst saving Json config", e);
        }
	    return this;
	}

	public void save() {
	    save(path);
	}
}
