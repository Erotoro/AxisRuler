package dev.erotoro.axisruler.core.config;

import dev.erotoro.axisruler.core.util.ModConstants;
import java.util.Locale;

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

    public String translationKey() {
        return ModConstants.MOD_ID + ".config.anchor." + name().toLowerCase(Locale.ROOT);
    }
}
