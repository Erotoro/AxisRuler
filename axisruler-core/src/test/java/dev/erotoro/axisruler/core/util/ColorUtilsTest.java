package dev.erotoro.axisruler.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ColorUtilsTest {
    @Test
    void parseHexAcceptsAllSupportedForms() {
        assertEquals(0xFF43D98C, ColorUtils.parseHex("#43D98C", 0));
        assertEquals(0xFF43D98C, ColorUtils.parseHex("43D98C", 0));
        assertEquals(0x8043D98C, ColorUtils.parseHex("#8043D98C", 0));
        assertEquals(0x8043D98C, ColorUtils.parseHex("8043D98C", 0));
    }

    @Test
    void parseHexReturnsFallbackForGarbage() {
        assertEquals(123, ColorUtils.parseHex("zzz", 123));
        assertEquals(123, ColorUtils.parseHex(null, 123));
    }

    @Test
    void toHexRoundTrips() {
        int argb = 0x8043D98C;
        assertEquals(argb, ColorUtils.parseHex(ColorUtils.toHex(argb, true), 0));
    }

    @Test
    void channelAccessorsDecomposeArgb() {
        int argb = 0x8043D98C;
        assertEquals(0x80, ColorUtils.alpha(argb));
        assertEquals(0x43, ColorUtils.red(argb));
        assertEquals(0xD9, ColorUtils.green(argb));
        assertEquals(0x8C, ColorUtils.blue(argb));
    }

    @Test
    void hsvRoundTripsApproximately() {
        int argb = ColorUtils.rgb(120, 200, 90);
        float[] hsv = ColorUtils.rgbToHsv(argb);
        int back = ColorUtils.hsvToRgb(hsv[0], hsv[1], hsv[2]);
        assertEquals(ColorUtils.red(argb), ColorUtils.red(back), 2);
        assertEquals(ColorUtils.green(argb), ColorUtils.green(back), 2);
        assertEquals(ColorUtils.blue(argb), ColorUtils.blue(back), 2);
    }
}
