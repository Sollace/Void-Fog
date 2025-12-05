package com.tamaized.voidfog.modmenu;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.AbstractSlider;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Slider;
import com.minelittlepony.common.client.gui.element.Toggle;
import com.tamaized.voidfog.Settings;
import com.tamaized.voidfog.VoidFog;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

class OptionsScreen extends GameGui {
    private static final Component TITLE = Component.translatable("menu.voidfog.title");
    private static final Component PARTICLES_MIN = Component.translatable("menu.voidfog.particles.min");
    private static final Component PARTICLES_MAX = Component.translatable("menu.voidfog.particles.max");
    private static final Component PARTICLES_DEF = Component.translatable("menu.voidfog.particles.default");

    private static final Component FOG_HEIGHT_DEFAULT = Component.translatable("menu.voidfog.fog_height.default");

    private static final Component DENSITY_MIN = Component.translatable("menu.voidfog.density.min");
    private static final Component DENSITY_MAX = Component.translatable("menu.voidfog.density.max");

    private final ScrollContainer content = new ScrollContainer();

    public OptionsScreen(@Nullable Screen parent) {
        super(TITLE, parent);

        content.margin.top = 30;
        content.margin.bottom = 30;
        content.getContentPadding().top = 10;
        content.getContentPadding().right = 10;
        content.getContentPadding().bottom = 20;
        content.getContentPadding().left = 10;
    }

    @Override
    public void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {
        int LEFT = content.width / 2 - 110;

        int row = 0;

        Settings config = VoidFog.config;

        getChildElements().add(content);

        addButton(new Label(width / 2, 5).setCentered()).getStyle().setText(getTitle().getString());

        content.addButton(new Slider(LEFT, row += 10, 0, 10000, config.voidParticleDensity))
            .onChange(config::setParticleDensity)
            .setTextFormat(this::formatValue);

        content.addButton(new Slider(LEFT, row += 30, 0, 100, config.fogDensity))
            .onChange(config::setFogDensity)
            .setTextFormat(this::formatFogDensity);

        content.addButton(new Slider(LEFT, row += 30, 0, 383, config.maxFogHeight))
            .onChange(config::setFogHeight)
            .setTextFormat(this::formatFogHeight);

        content.addButton(new Toggle(LEFT, row += 30, config.enabled))
            .onChange(config.enabled)
            .getStyle()
                .setText("menu.voidfog.enabled");

        content.addButton(new Toggle(LEFT, row += 25, config.prettyFog))
                .onChange(config.prettyFog)
                .getStyle()
                    .setTooltip("menu.voidfog.prettyFog.tooltip")
                    .setText("menu.voidfog.prettyFog");

        content.addButton(new Toggle(LEFT, row += 25, config.scaleWithDifficulty))
            .onChange(config.scaleWithDifficulty)
            .getStyle()
                .setTooltip("menu.voidfog.scale.tooltip")
                .setText("menu.voidfog.scale");

        content.addButton(new Toggle(LEFT, row += 25, config.disableInCreative))
            .onChange(config.disableInCreative)
            .getStyle()
                .setText("menu.voidfog.creative");

        content.addButton(new Toggle(LEFT, row += 25, config.respectTorches))
            .onChange(config.respectTorches)
            .getStyle()
                .setTooltip("menu.voidfog.torches.tooltip")
                .setText("menu.voidfog.torches");

        content.addButton(new Toggle(LEFT, row += 25, config.imABigBoi))
            .onChange(config.imABigBoi)
            .getStyle()
                .setTooltip("menu.voidfog.bigboi.tooltip")
                .setText("menu.voidfog.bigboi");

        addButton(new Button(width / 2 - 100, height - 25).onClick(sender -> finish())).getStyle().setText("gui.done");
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
        content.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public void onClose() {
        VoidFog.config.save();
        super.onClose();
    }

    private Component formatValue(AbstractSlider<Float> sender) {
        float value = sender.getValue();

        if (value <= 0) {
            return PARTICLES_MIN;
        }

        if (value >= 10000) {
            return PARTICLES_MAX;
        }

        if (value == 1000) {
            return PARTICLES_DEF;
        }

        return Component.translatable("menu.voidfog.particles", (int)Math.floor(value));
    }

    private Component formatFogDensity(AbstractSlider<Float> sender) {
        float value = sender.getValue();

        if (value <= 0) {
            return DENSITY_MIN;
        }

        if (value >= 100) {
            return DENSITY_MAX;
        }

        return Component.translatable("menu.voidfog.density", (int)Math.floor(value));
    }

    private Component formatFogHeight(AbstractSlider<Float> sender) {
        if (sender.getValue() == 32) {
            return FOG_HEIGHT_DEFAULT;
        }
        return Component.translatable("menu.voidfog.fogheight", (int)(double)(sender.getValue()) - 64);
    }
}
