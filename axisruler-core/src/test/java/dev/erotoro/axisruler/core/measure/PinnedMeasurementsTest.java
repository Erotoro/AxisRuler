package dev.erotoro.axisruler.core.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PinnedMeasurementsTest {
    private static MeasurePoint p(String world, int x, int y, int z) {
        return MeasurePoint.of(world, x, y, z);
    }

    @Test
    void pinStoresEntryAndBumpsRevision() {
        PinnedMeasurements store = new PinnedMeasurements();
        int before = store.revision();

        Pinned entry = store.pin(p("minecraft:overworld", 0, 0, 0), p("minecraft:overworld", 4, 2, 6));

        assertNotNull(entry);
        assertEquals(1, store.count());
        assertEquals("minecraft:overworld", entry.worldKey());
        assertTrue(store.revision() > before);
    }

    @Test
    void coloursCycleThroughPalette() {
        PinnedMeasurements store = new PinnedMeasurements();
        int first = store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1)).colorArgb();
        int second = store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1)).colorArgb();
        assertEquals(MeasurementPalette.color(0), first);
        assertEquals(MeasurementPalette.color(1), second);
    }

    @Test
    void differentWorldsRejected() {
        PinnedMeasurements store = new PinnedMeasurements();
        assertThrows(IllegalArgumentException.class,
                () -> store.pin(p("minecraft:overworld", 0, 0, 0), p("minecraft:the_nether", 1, 1, 1)));
    }

    @Test
    void worldFilterIsolatesEntries() {
        PinnedMeasurements store = new PinnedMeasurements();
        store.pin(p("minecraft:overworld", 0, 0, 0), p("minecraft:overworld", 1, 1, 1));
        store.pin(p("minecraft:the_nether", 0, 0, 0), p("minecraft:the_nether", 1, 1, 1));

        assertEquals(2, store.count());
        assertEquals(1, store.countForWorld("minecraft:overworld"));
        assertEquals(1, store.listForWorld("minecraft:the_nether").size());
    }

    @Test
    void clearReturnsRemovedCountAndResetsColours() {
        PinnedMeasurements store = new PinnedMeasurements();
        store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1));
        store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1));

        assertEquals(2, store.clear());
        assertTrue(store.isEmpty());
        assertEquals(0, store.clear());
        // colour cursor reset -> first colour again
        assertEquals(MeasurementPalette.color(0), store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1)).colorArgb());
    }

    @Test
    void respectsCapacityCap() {
        PinnedMeasurements store = new PinnedMeasurements();
        for (int i = 0; i < PinnedMeasurements.MAX_PINNED; i++) {
            assertNotNull(store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1)));
        }
        assertTrue(store.isFull());
        assertNull(store.pin(p("w", 0, 0, 0), p("w", 1, 1, 1)));
        assertEquals(PinnedMeasurements.MAX_PINNED, store.count());
    }

    @Test
    void selectionStateExposesPreviewAndPinned() {
        SelectionState state = new SelectionState();
        assertNotNull(state.pinned());
        assertFalse(state.hasLivePreview());

        state.setPointA(p("w", 0, 0, 0));
        state.setPreviewTarget(p("w", 5, 0, 5));
        assertTrue(state.hasLivePreview());

        state.setPointB(p("w", 9, 9, 9));
        assertFalse(state.hasLivePreview(), "no preview once B is set");
    }
}
