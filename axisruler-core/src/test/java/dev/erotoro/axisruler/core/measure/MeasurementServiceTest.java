package dev.erotoro.axisruler.core.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.erotoro.axisruler.core.geometry.BlockPoint;
import org.junit.jupiter.api.Test;

class MeasurementServiceTest {
    private final MeasurementService service = new MeasurementService(new SelectionState());

    @Test
    void inclusiveSizeCountsBothEndpoints() {
        MeasurePoint a = MeasurePoint.of("minecraft:overworld", 0, 0, 0);
        MeasurePoint b = MeasurePoint.of("minecraft:overworld", 3, 1, 4);

        MeasurementResult result = service.calculate(a, b);

        assertTrue(result.valid());
        assertEquals(4, result.sizeX());
        assertEquals(2, result.sizeY());
        assertEquals(5, result.sizeZ());
        assertEquals(40L, result.volume());
        assertEquals(20L, result.floorArea());
    }

    @Test
    void deltasAndDistancesAreCorrect() {
        MeasurePoint a = MeasurePoint.of("minecraft:overworld", 1, 2, 3);
        MeasurePoint b = MeasurePoint.of("minecraft:overworld", 4, 6, 3);

        MeasurementResult result = service.calculate(a, b);

        assertEquals(3, result.dx());
        assertEquals(4, result.dy());
        assertEquals(0, result.dz());
        assertEquals(7, result.manhattanDistance());
        assertEquals(5.0D, result.euclideanDistance(), 1.0E-9D);
    }

    @Test
    void boundsAreOrderIndependent() {
        MeasurePoint a = MeasurePoint.of("minecraft:overworld", 5, 9, 2);
        MeasurePoint b = MeasurePoint.of("minecraft:overworld", 1, 3, 8);

        MeasurementResult result = service.calculate(a, b);

        assertEquals(new BlockPoint(1, 3, 2), result.minBlockPos());
        assertEquals(new BlockPoint(5, 9, 8), result.maxBlockPos());
        assertEquals(3.5D, result.center().x(), 1.0E-9D);
    }

    @Test
    void differentWorldsAreInvalid() {
        MeasurePoint a = MeasurePoint.of("minecraft:overworld", 0, 0, 0);
        MeasurePoint b = MeasurePoint.of("minecraft:the_nether", 0, 0, 0);

        MeasurementResult result = service.calculate(a, b);

        assertFalse(result.valid());
    }

    @Test
    void missingPointIsInvalid() {
        assertFalse(service.calculate(null, null).valid());
    }
}
