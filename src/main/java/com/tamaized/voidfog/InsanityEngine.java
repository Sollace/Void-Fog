package com.tamaized.voidfog;

import com.tamaized.voidfog.api.Voidable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class InsanityEngine {
    private int timeToNextSound = 0;
    private int insanityBuildUp;

    private final Sound[] events = new Sound[] {
            Sound.of(SoundEvents.POLAR_BEAR_WARNING),
            Sound.of(SoundEvents.AMBIENT_CAVE),
            Sound.of(SoundEvents.CREEPER_PRIMED),
            Sound.of(SoundEvents.ZOMBIE_DESTROY_EGG),
            Sound.of(SoundEvents.CHEST_CLOSE),
            Sound.of(SoundEvents.UI_TOAST_IN),
            Sound.of(SoundEvents.COMPOSTER_READY),
            Sound.of(SoundEvents.METAL_STEP),
            Sound.of(SoundEvents.UI_BUTTON_CLICK),
            Sound.of(SoundEvents.ZOGLIN_ANGRY),
            Sound.of(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON),
            Sound.of(SoundEvents.ZOMBIE_STEP)
    };

    public void update(Level world, Entity entity, Voidable dimension) {

        if (!dimension.hasInsanity(BlockPos.containing(entity.getEyePosition()), world)) {
            return;
        }

        float brightness = Luminance.getLuminance(entity, true);

        if (brightness > 0.3F) {
            insanityBuildUp = 0;
            return;
        }

        double y = entity.getEyeY();
        int rarity = getRarity(y, world);

        insanityBuildUp += y < 0 ? -y : 1;

        if (insanityBuildUp > 100) {
            timeToNextSound -= insanityBuildUp / 60;
        }

        if (timeToNextSound-- > 0) {
            return;
        }

        timeToNextSound = 20 + rarity + world.getRandom().nextInt(
                Math.max(250, 120 + rarity)
        );

        doAScary(world, entity.blockPosition());
    }

    private int getRarity(double y, Level world) {
        // higher value = lower probability
        // max ---- y -0-- min
        y -= world.getMinY();
        y ++;
        return 1000 * (int)y;
    }

    private void doAScary(Level world, BlockPos pos) {
        Sound event = events[world.getRandom().nextInt(events.length)];
        float pitch = 1 + world.getRandom().nextFloat();
        event.play(world, pos, 1, pitch);
    }

    interface Sound {
        static Sound of(SoundEvent event) {
            return (world, pos, volume, pitch) -> {
                world.playSound(Minecraft.getInstance().player, pos, event, SoundSource.AMBIENT, volume, pitch);
            };
        }

        static Sound of(Holder<SoundEvent> event) {
            return (world, pos, volume, pitch) -> {
                world.playSeededSound(Minecraft.getInstance().player, pos.getX(), pos.getY(), pos.getZ(), event, SoundSource.AMBIENT, volume, pitch, world.getRandom().nextLong());
            };
        }

        void play(Level world, BlockPos pos, float volume, float pitch);
    }
}
