package com.axisruler.measure;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record MeasurementResult(
        boolean valid,
        String invalidReason,
        Optional<MeasurePoint> pointA,
        Optional<MeasurePoint> pointB,
        String worldKey,
        int dx,
        int dy,
        int dz,
        int absDx,
        int absDy,
        int absDz,
        int sizeX,
        int sizeY,
        int sizeZ,
        double euclideanDistance,
        int manhattanDistance,
        long floorArea,
        long wallAreaXY,
        long wallAreaYZ,
        long volume,
        BlockPos minBlockPos,
        BlockPos maxBlockPos,
        Vec3 center
) {
    private static final BlockPos ZERO_BLOCK_POS = new BlockPos(0, 0, 0);
    private static final Vec3 ZERO_CENTER = new Vec3(0.0, 0.0, 0.0);
    private static final String EMPTY_WORLD_KEY = "";

    public MeasurementResult {
        invalidReason = Objects.requireNonNull(invalidReason, "invalidReason");
        pointA = Objects.requireNonNull(pointA, "pointA");
        pointB = Objects.requireNonNull(pointB, "pointB");
        worldKey = Objects.requireNonNull(worldKey, "worldKey");
        minBlockPos = copy(Objects.requireNonNull(minBlockPos, "minBlockPos"));
        maxBlockPos = copy(Objects.requireNonNull(maxBlockPos, "maxBlockPos"));
        center = Objects.requireNonNull(center, "center");
        if (valid && !invalidReason.isEmpty()) {
            throw new IllegalArgumentException("valid measurement must not have an invalid reason");
        }
        if (valid && (pointA.isEmpty() || pointB.isEmpty())) {
            throw new IllegalArgumentException("valid measurement requires both points");
        }
    }

    public static MeasurementResult invalid(String reason, MeasurePoint pointA, MeasurePoint pointB) {
        String safeReason = Objects.requireNonNull(reason, "reason").trim();
        if (safeReason.isEmpty()) {
            throw new IllegalArgumentException("invalid measurement reason must not be blank");
        }
        return new MeasurementResult(
                false,
                safeReason,
                Optional.ofNullable(pointA),
                Optional.ofNullable(pointB),
                resolveWorldKey(pointA, pointB),
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0.0,
                0,
                0L,
                0L,
                0L,
                0L,
                resolveMinBlockPos(pointA, pointB),
                resolveMaxBlockPos(pointA, pointB),
                ZERO_CENTER
        );
    }

    public static MeasurementResult valid(
            MeasurePoint pointA,
            MeasurePoint pointB,
            int dx,
            int dy,
            int dz,
            int absDx,
            int absDy,
            int absDz,
            int sizeX,
            int sizeY,
            int sizeZ,
            double euclideanDistance,
            int manhattanDistance,
            long floorArea,
            long wallAreaXY,
            long wallAreaYZ,
            long volume,
            BlockPos minBlockPos,
            BlockPos maxBlockPos,
            Vec3 center
    ) {
        return new MeasurementResult(
                true,
                "",
                Optional.of(Objects.requireNonNull(pointA, "pointA")),
                Optional.of(Objects.requireNonNull(pointB, "pointB")),
                pointA.worldKey(),
                dx,
                dy,
                dz,
                absDx,
                absDy,
                absDz,
                sizeX,
                sizeY,
                sizeZ,
                euclideanDistance,
                manhattanDistance,
                floorArea,
                wallAreaXY,
                wallAreaYZ,
                volume,
                minBlockPos,
                maxBlockPos,
                center
        );
    }

    public boolean complete() {
        return pointA.isPresent() && pointB.isPresent();
    }

    public MeasurePoint pointAOrNull() {
        return pointA.orElse(null);
    }

    public MeasurePoint pointBOrNull() {
        return pointB.orElse(null);
    }

    public boolean sameWorld() {
        return complete() && pointA.get().worldKey().equals(pointB.get().worldKey());
    }

    public String formatDimensions() {
        return MeasurementFormatter.formatDimensions(sizeX, sizeY, sizeZ);
    }

    public String formatSignedDelta() {
        return MeasurementFormatter.formatSignedDelta(dx, dy, dz);
    }

    public String formatAbsoluteDelta() {
        return MeasurementFormatter.formatAbsoluteDelta(absDx, absDy, absDz);
    }

    public String formatDistanceSummary() {
        return "Euclidean=" + MeasurementFormatter.formatDecimal(euclideanDistance)
                + ", Manhattan=" + manhattanDistance;
    }

    public String formatCenter() {
        return MeasurementFormatter.formatVec3(center);
    }

    public String formatBounds() {
        return MeasurementFormatter.formatBlockPos(minBlockPos) + " -> " + MeasurementFormatter.formatBlockPos(maxBlockPos);
    }

    public List<String> toHudLines() {
        if (!valid) {
            return List.of("Measurement unavailable", invalidReason);
        }
        return List.of(
                "Size: " + formatDimensions(),
                "Delta: " + formatSignedDelta(),
                "Distance: " + formatDistanceSummary(),
                "Floor XZ: " + floorArea,
                "Walls XY/YZ: " + wallAreaXY + " / " + wallAreaYZ,
                "Volume: " + volume,
                "Center: " + formatCenter()
        );
    }

    private static String resolveWorldKey(MeasurePoint pointA, MeasurePoint pointB) {
        if (pointA != null) {
            return pointA.worldKey();
        }
        if (pointB != null) {
            return pointB.worldKey();
        }
        return EMPTY_WORLD_KEY;
    }

    private static BlockPos resolveMinBlockPos(MeasurePoint pointA, MeasurePoint pointB) {
        if (pointA == null && pointB == null) {
            return ZERO_BLOCK_POS;
        }
        if (pointA == null) {
            return pointB.blockPos();
        }
        if (pointB == null) {
            return pointA.blockPos();
        }
        return new BlockPos(
                Math.min(pointA.x(), pointB.x()),
                Math.min(pointA.y(), pointB.y()),
                Math.min(pointA.z(), pointB.z())
        );
    }

    private static BlockPos resolveMaxBlockPos(MeasurePoint pointA, MeasurePoint pointB) {
        if (pointA == null && pointB == null) {
            return ZERO_BLOCK_POS;
        }
        if (pointA == null) {
            return pointB.blockPos();
        }
        if (pointB == null) {
            return pointA.blockPos();
        }
        return new BlockPos(
                Math.max(pointA.x(), pointB.x()),
                Math.max(pointA.y(), pointB.y()),
                Math.max(pointA.z(), pointB.z())
        );
    }

    private static BlockPos copy(BlockPos blockPos) {
        return new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}
