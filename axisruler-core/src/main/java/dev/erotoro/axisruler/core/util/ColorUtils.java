package dev.erotoro.axisruler.core.util;

import java.util.Locale;

public final class ColorUtils {
    private ColorUtils() {
    }

    public static int clampChannel(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static float clampUnit(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public static int withAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | (clampChannel(alpha) << 24);
    }

    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static int red(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    public static int green(int argb) {
        return (argb >>> 8) & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (clampChannel(red) << 16) | (clampChannel(green) << 8) | clampChannel(blue);
    }

    public static int parseHex(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim().toUpperCase(Locale.ROOT);
        if (trimmed.matches("#[0-9A-F]{6}")) {
            return (int) Long.parseLong("FF" + trimmed.substring(1), 16);
        }
        if (trimmed.matches("#[0-9A-F]{8}")) {
            return (int) Long.parseLong(trimmed.substring(1), 16);
        }
        if (trimmed.matches("[0-9A-F]{6}")) {
            return (int) Long.parseLong("FF" + trimmed, 16);
        }
        if (trimmed.matches("[0-9A-F]{8}")) {
            return (int) Long.parseLong(trimmed, 16);
        }
        return fallback;
    }

    public static String toHex(int argb, boolean includeAlpha) {
        return includeAlpha
                ? String.format(Locale.ROOT, "#%08X", argb)
                : String.format(Locale.ROOT, "#%06X", argb & 0x00FFFFFF);
    }

    public static float[] rgbToHsv(int argb) {
        float red = red(argb) / 255.0F;
        float green = green(argb) / 255.0F;
        float blue = blue(argb) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;
        float hue;

        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == red) {
            hue = ((green - blue) / delta) % 6.0F;
        } else if (max == green) {
            hue = ((blue - red) / delta) + 2.0F;
        } else {
            hue = ((red - green) / delta) + 4.0F;
        }

        hue *= 60.0F;
        if (hue < 0.0F) {
            hue += 360.0F;
        }

        float saturation = max == 0.0F ? 0.0F : delta / max;
        float value = max;
        return new float[]{hue, saturation, value};
    }

    public static int hsvToRgb(float hue, float saturation, float value) {
        saturation = clampUnit(saturation);
        value = clampUnit(value);
        float normalizedHue = ((hue % 360.0F) + 360.0F) % 360.0F;
        float chroma = value * saturation;
        float sector = normalizedHue / 60.0F;
        float x = chroma * (1.0F - Math.abs(sector % 2.0F - 1.0F));
        float match = value - chroma;
        float red;
        float green;
        float blue;

        if (sector < 1.0F) {
            red = chroma;
            green = x;
            blue = 0.0F;
        } else if (sector < 2.0F) {
            red = x;
            green = chroma;
            blue = 0.0F;
        } else if (sector < 3.0F) {
            red = 0.0F;
            green = chroma;
            blue = x;
        } else if (sector < 4.0F) {
            red = 0.0F;
            green = x;
            blue = chroma;
        } else if (sector < 5.0F) {
            red = x;
            green = 0.0F;
            blue = chroma;
        } else {
            red = chroma;
            green = 0.0F;
            blue = x;
        }

        return rgb(
                Math.round((red + match) * 255.0F),
                Math.round((green + match) * 255.0F),
                Math.round((blue + match) * 255.0F)
        );
    }
}
