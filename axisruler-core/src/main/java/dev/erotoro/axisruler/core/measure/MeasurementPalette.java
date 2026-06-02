package dev.erotoro.axisruler.core.measure;

/**
 * Distinct colour cycle used to auto-assign colours to pinned measurements so that several
 * pinned boxes stay visually separable without the user having to choose a colour.
 */
public final class MeasurementPalette {
    private static final int[] COLORS = {
            0xFF43D98C, // green
            0xFFFF8A5B, // orange
            0xFF8FD8F7, // sky
            0xFFFFD364, // amber
            0xFFB892FF, // violet
            0xFF6AF0B0, // mint
            0xFFFF6B8B, // rose
            0xFF7BD8FF  // cyan
    };

    private MeasurementPalette() {
    }

    public static int color(int index) {
        return COLORS[Math.floorMod(index, COLORS.length)];
    }

    public static int size() {
        return COLORS.length;
    }
}
