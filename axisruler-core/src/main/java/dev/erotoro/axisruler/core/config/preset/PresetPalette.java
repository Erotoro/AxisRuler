package dev.erotoro.axisruler.core.config.preset;

import dev.erotoro.axisruler.core.config.LabelBackgroundMode;

/**
 * Immutable colour/visual payload for a built-in style preset. Defined once in the core
 * so every version module renders identical presets; platform config screens copy these
 * values into their editing buffer when a preset is applied.
 */
public record PresetPalette(
        int pointAColor,
        int pointBColor,
        int boxColor,
        int connectionLineColor,
        int xGuideColor,
        int yGuideColor,
        int zGuideColor,
        int labelColor,
        int labelBackgroundColor,
        int hudTitleColor,
        int hudPrimaryTextColor,
        int hudSecondaryTextColor,
        int hudAccentColor,
        int hudBackgroundColor,
        int hudBorderColor,
        int hudWarningColor,
        int hudBackgroundAlpha,
        int hudBorderAlpha,
        boolean hudTextShadow,
        LabelBackgroundMode labelBackgroundMode,
        float labelScale,
        float lineThickness,
        float calloutOffset,
        float tickSize,
        int boxFillAlpha,
        int guideAlpha,
        int lineAlpha
) {
}
