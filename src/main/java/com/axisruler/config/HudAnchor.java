package com.axisruler.config;

import java.util.Locale;
import net.minecraft.network.chat.Component;

public enum HudAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public static HudAnchor fromName(String value) {
        if (value == null) {
            return TOP_LEFT;
        }
        try {
            return HudAnchor.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return TOP_LEFT;
        }
    }

    public String configValue() {
        return name();
    }

    public HudAnchor next() {
        HudAnchor[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public Component localized() {
        return Component.translatable("axisruler.config.anchor." + name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return localized().getString();
    }
}
