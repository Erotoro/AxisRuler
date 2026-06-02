package dev.erotoro.axisruler.core.measure;

import java.util.Objects;

/**
 * An immutable, frozen measurement that the user pinned to keep on screen. Carries only what a
 * static box needs to render: the two endpoints, the world they belong to, and an auto-assigned
 * colour. No name/visibility/per-item style by design — pinned measurements stay lightweight.
 */
public record Pinned(MeasurePoint a, MeasurePoint b, String worldKey, int colorArgb) {
    public Pinned {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        Objects.requireNonNull(worldKey, "worldKey");
    }
}
