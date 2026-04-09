package com.axisruler.config;

import java.util.Locale;
import net.minecraft.text.Text;

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

    public Text localized() {
        return Text.translatable("axisruler.config.background." + name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return localized().getString();
    }
}
