package com.axisruler.measure;

import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class MeasurementFormatter {
    private MeasurementFormatter() {
    }

    public static String formatDimensions(int sizeX, int sizeY, int sizeZ) {
        return sizeX + " x " + sizeY + " x " + sizeZ;
    }

    public static String formatSignedDelta(int dx, int dy, int dz) {
        return "dx=" + dx + ", dy=" + dy + ", dz=" + dz;
    }

    public static String formatAbsoluteDelta(int absDx, int absDy, int absDz) {
        return "x=" + absDx + ", y=" + absDy + ", z=" + absDz;
    }

    public static String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public static String formatBlockPos(BlockPos blockPos) {
        Objects.requireNonNull(blockPos, "blockPos");
        return blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
    }

    public static String formatVec3(Vec3d vec3) {
        Objects.requireNonNull(vec3, "vec3");
        return formatDecimal(vec3.x) + ", " + formatDecimal(vec3.y) + ", " + formatDecimal(vec3.z);
    }
}
