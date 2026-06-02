package dev.erotoro.axisruler.core.measure;

import dev.erotoro.axisruler.core.util.ModConstants;
import java.util.Locale;

public enum SelectionMode {
    BOX,
    SURFACE,
    LINE,
    AXIS,
    MINIMAL;

    public SelectionMode next() {
        SelectionMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String translationKey() {
        return ModConstants.MOD_ID + ".mode." + name().toLowerCase(Locale.ROOT);
    }
}
