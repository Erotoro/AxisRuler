package com.axisruler.measure;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record MeasurePoint(String worldKey, BlockPos blockPos) {
    public MeasurePoint {
        worldKey = requireWorldKey(worldKey);
        Objects.requireNonNull(blockPos, "blockPos");
        blockPos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static MeasurePoint of(String worldKey, BlockPos blockPos) {
        return new MeasurePoint(worldKey, blockPos);
    }

    public static MeasurePoint of(ResourceKey<Level> worldKey, BlockPos blockPos) {
        Objects.requireNonNull(worldKey, "worldKey");
        return new MeasurePoint(worldKey.identifier().toString(), blockPos);
    }

    public int x() {
        return blockPos.getX();
    }

    public int y() {
        return blockPos.getY();
    }

    public int z() {
        return blockPos.getZ();
    }

    public Vec3 blockCenter() {
        return new Vec3(x() + 0.5D, y() + 0.5D, z() + 0.5D);
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
