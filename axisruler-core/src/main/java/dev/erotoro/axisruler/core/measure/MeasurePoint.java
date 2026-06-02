package dev.erotoro.axisruler.core.measure;

import dev.erotoro.axisruler.core.geometry.BlockPoint;
import dev.erotoro.axisruler.core.geometry.Vec3d;
import java.util.Objects;

public record MeasurePoint(String worldKey, BlockPoint blockPos) {
    public MeasurePoint {
        worldKey = requireWorldKey(worldKey);
        Objects.requireNonNull(blockPos, "blockPos");
    }

    public static MeasurePoint of(String worldKey, BlockPoint blockPos) {
        return new MeasurePoint(worldKey, blockPos);
    }

    public static MeasurePoint of(String worldKey, int x, int y, int z) {
        return new MeasurePoint(worldKey, new BlockPoint(x, y, z));
    }

    public int x() {
        return blockPos.x();
    }

    public int y() {
        return blockPos.y();
    }

    public int z() {
        return blockPos.z();
    }

    public Vec3d blockCenter() {
        return new Vec3d(x() + 0.5D, y() + 0.5D, z() + 0.5D);
    }

    public String formatBlockPosition() {
        return x() + " " + y() + " " + z();
    }

    public String formatForHud() {
        return formatBlockPosition() + " [" + worldKey + "]";
    }

    private static String requireWorldKey(String worldKey) {
        Objects.requireNonNull(worldKey, "worldKey");
        String trimmed = worldKey.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("worldKey must not be blank");
        }
        return trimmed;
    }
}
