package dev.erotoro.axisruler.core.geometry;

/**
 * Immutable integer block coordinate, decoupled from any Minecraft mapping so the
 * measurement core stays platform independent. Each version module adapts its own
 * {@code BlockPos} into this type at the input boundary.
 */
public record BlockPoint(int x, int y, int z) {
    public static BlockPoint min(BlockPoint a, BlockPoint b) {
        return new BlockPoint(
                Math.min(a.x, b.x),
                Math.min(a.y, b.y),
                Math.min(a.z, b.z)
        );
    }

    public static BlockPoint max(BlockPoint a, BlockPoint b) {
        return new BlockPoint(
                Math.max(a.x, b.x),
                Math.max(a.y, b.y),
                Math.max(a.z, b.z)
        );
    }
}
