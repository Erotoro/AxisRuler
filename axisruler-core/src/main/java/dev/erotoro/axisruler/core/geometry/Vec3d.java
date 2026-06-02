package dev.erotoro.axisruler.core.geometry;

/**
 * Immutable double-precision vector used for measurement geometry (centres, distances).
 * Mapping independent so the core can compute without referencing Minecraft's {@code Vec3}.
 */
public record Vec3d(double x, double y, double z) {
    public double distanceTo(Vec3d other) {
        return Math.sqrt(distanceSquaredTo(other));
    }

    public double distanceSquaredTo(Vec3d other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
