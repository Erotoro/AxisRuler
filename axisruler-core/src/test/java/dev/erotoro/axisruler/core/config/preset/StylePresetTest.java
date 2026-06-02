package dev.erotoro.axisruler.core.config.preset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StylePresetTest {
    @Test
    void builtInPresetsCarryAPalette() {
        for (StylePreset preset : StylePreset.values()) {
            if (preset == StylePreset.CUSTOM_SAVED) {
                continue;
            }
            assertTrue(preset.hasPalette(), preset + " should expose a palette");
            assertNotNull(preset.palette());
        }
    }

    @Test
    void customSavedHasNoBuiltInPalette() {
        assertFalse(StylePreset.CUSTOM_SAVED.hasPalette());
    }

    @Test
    void classicPaletteMatchesKnownValues() {
        PresetPalette classic = StylePreset.CLASSIC.palette();
        assertEquals(0xFF35D07F, classic.pointAColor());
        assertEquals(0xFFE84C4C, classic.pointBColor());
        assertEquals(192, classic.lineAlpha());
    }
}
