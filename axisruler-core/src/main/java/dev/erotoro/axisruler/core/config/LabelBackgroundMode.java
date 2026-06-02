package dev.erotoro.axisruler.core.config;

import dev.erotoro.axisruler.core.util.ModConstants;
import java.util.Locale;

public enum LabelBackgroundMode {
    NONE,
    SUBTLE,
    SOLID;

    public static LabelBackgroundMode fromName(String value) {
        if (value == null) {
            return SUBTLE;
        }
        try {
            return LabelBackgroundMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return SUBTLE;
        }
    }

    public String configValue() {
        return name();
    }

    public LabelBackgroundMode next() {
        LabelBackgroundMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String translationKey() {
        return ModConstants.MOD_ID + ".config.background." + name().toLowerCase(Locale.ROOT);
    }
}
