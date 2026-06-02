package dev.erotoro.axisruler.core.measure;

import dev.erotoro.axisruler.core.geometry.BlockPoint;
import dev.erotoro.axisruler.core.geometry.Vec3d;
import java.util.Objects;

public final class MeasurementService {
    private final SelectionState selectionState;

    public MeasurementService(SelectionState selectionState) {
        this.selectionState = Objects.requireNonNull(selectionState, "selectionState");
    }

    public MeasurementResult calculate() {
        return calculate(selectionState);
    }

    public MeasurementResult calculate(SelectionState selectionState) {
        Objects.requireNonNull(selectionState, "selectionState");
        return calculate(selectionState.pointAOrNull(), selectionState.pointBOrNull());
    }

    public MeasurementResult calculate(MeasurePoint pointA, MeasurePoint pointB) {
        if (pointA == null || pointB == null) {
            return MeasurementResult.invalid("Both Point A and Point B must be selected.", pointA, pointB);
        }
        if (!pointA.worldKey().equals(pointB.worldKey())) {
            return MeasurementResult.invalid(
                    "Point A and Point B are in different worlds: " + pointA.worldKey() + " != " + pointB.worldKey() + ".",
                    pointA,
                    pointB
            );
        }

        int dx = delta(pointA.x(), pointB.x());
        int dy = delta(pointA.y(), pointB.y());
        int dz = delta(pointA.z(), pointB.z());
        int absDx = absolute(dx);
        int absDy = absolute(dy);
        int absDz = absolute(dz);
        int sizeX = inclusiveSize(absDx);
        int sizeY = inclusiveSize(absDy);
        int sizeZ = inclusiveSize(absDz);
        double euclideanDistance = euclidean(dx, dy, dz);
        int manhattanDistance = manhattan(absDx, absDy, absDz);
        long floorArea = multiply(sizeX, sizeZ);
        long wallAreaXY = multiply(sizeX, sizeY);
        long wallAreaYZ = multiply(sizeY, sizeZ);
        long volume = multiply(sizeX, sizeY, sizeZ);
        BlockPoint minBlockPos = BlockPoint.min(pointA.blockPos(), pointB.blockPos());
        BlockPoint maxBlockPos = BlockPoint.max(pointA.blockPos(), pointB.blockPos());
        Vec3d center = inclusiveCenter(minBlockPos, sizeX, sizeY, sizeZ);

        return MeasurementResult.valid(
                pointA,
                pointB,
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

    public boolean hasSelection() {
        return selectionState.pointA().isPresent() || selectionState.pointB().isPresent();
    }

    public boolean hasCompleteSelection() {
        return selectionState.complete();
    }

    public SelectionState selectionState() {
        return selectionState;
    }

    private static int delta(int from, int to) {
        return to - from;
    }

    private static int absolute(int value) {
        return Math.abs(value);
    }

    private static int inclusiveSize(int absoluteDelta) {
        return absoluteDelta + 1;
    }

    private static double euclidean(int dx, int dy, int dz) {
        return Math.sqrt((double) dx * dx + (double) dy * dy + (double) dz * dz);
    }

    private static int manhattan(int absDx, int absDy, int absDz) {
        return absDx + absDy + absDz;
    }

    private static long multiply(int first, int second) {
        return (long) first * second;
    }

    private static long multiply(int first, int second, int third) {
        return (long) first * second * third;
    }

    private static Vec3d inclusiveCenter(BlockPoint minBlockPos, int sizeX, int sizeY, int sizeZ) {
        return new Vec3d(
                minBlockPos.x() + sizeX / 2.0D,
                minBlockPos.y() + sizeY / 2.0D,
                minBlockPos.z() + sizeZ / 2.0D
        );
    }
}
